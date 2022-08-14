package com.jozufozu.flywheel.backend.instancing.indirect;

import static org.lwjgl.opengl.GL46.*;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.RenderStage;
import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.struct.StorageBufferWriter;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.backend.instancing.PipelineCompiler;
import com.jozufozu.flywheel.core.Components;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.QuadConverter;
import com.jozufozu.flywheel.core.model.Mesh;
import com.jozufozu.flywheel.core.uniform.UniformBuffer;

public class IndirectList<T extends InstancedPart> {

	final StorageBufferWriter<T> storageBufferWriter;
	final GlProgram compute;
	final GlProgram draw;
	private final StructType<T> structType;
	private final VertexType vertexType;
	private final long objectStride;

	final IndirectBuffers buffers;

	final IndirectMeshPool meshPool;
	private final int elementBuffer;

	int vertexArray;

	final List<Batch<T>> batches = new ArrayList<>();

	IndirectList(StructType<T> structType, VertexType vertexType) {
		this.structType = structType;
		this.vertexType = vertexType;
		storageBufferWriter = this.structType.getStorageBufferWriter();

		objectStride = storageBufferWriter.getAlignment();
		buffers = new IndirectBuffers(objectStride);
		buffers.createBuffers();
		buffers.createObjectStorage(64 * 64 * 64);
		buffers.createDrawStorage(64);

		meshPool = new IndirectMeshPool(vertexType, 1024);

		vertexArray = glCreateVertexArrays();

		elementBuffer = QuadConverter.getInstance()
				.quads2Tris(2048).buffer.handle();
		setupVertexArray();

		var indirectShader = this.structType.getIndirectShader();
		compute = ComputeCullerCompiler.INSTANCE.get(indirectShader);
		draw = PipelineCompiler.INSTANCE.get(new PipelineCompiler.Context(vertexType, Materials.CHEST, indirectShader, Components.WORLD, Components.INDIRECT));
	}

	private void setupVertexArray() {
		glVertexArrayElementBuffer(vertexArray, elementBuffer);

		var meshLayout = vertexType.getLayout();
		var meshAttribs = meshLayout.getAttributeCount();

		var attributes = meshLayout.getAttributes();

		long offset = 0;
		for (int i = 0; i < meshAttribs; i++) {
			var attribute = attributes.get(i);
			glEnableVertexArrayAttrib(vertexArray, i);
			glVertexArrayVertexBuffer(vertexArray, i, meshPool.vbo, offset, meshLayout.getStride());
			attribute.format(vertexArray, i);
			offset += attribute
					.getByteWidth();
		}
	}

	public void add(IndirectInstancer<T> instancer, Material material, Mesh mesh) {
		batches.add(new Batch<>(instancer, material, meshPool.alloc(mesh)));
	}

	void submit(RenderStage stage) {
		if (batches.isEmpty()) {
			return;
		}
		int instanceCountThisFrame = calculateTotalInstanceCount();

		if (instanceCountThisFrame == 0) {
			return;
		}

		// TODO: Sort meshes by material and draw many contiguous sections of the draw indirect buffer,
		//  adjusting uniforms/textures accordingly
		buffers.updateCounts(instanceCountThisFrame, batches.size());
		meshPool.uploadAll();
		uploadInstanceData();
		uploadIndirectCommands();

		UniformBuffer.getInstance().sync();

		dispatchCompute(instanceCountThisFrame);
		glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);
		dispatchDraw();
	}

	private void dispatchDraw() {
		draw.bind();
		glVertexArrayElementBuffer(vertexArray, elementBuffer);
		glBindVertexArray(vertexArray);
		buffers.bindIndirectBuffer();

		final int stride = (int) IndirectBuffers.DRAW_COMMAND_STRIDE;
		long offset = 0;
		for (Batch<T> batch : batches) {

			batch.material.setup();
			glMultiDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_INT, offset, 1, stride);
			batch.material.clear();
			offset += stride;
		}
	}

	private void dispatchCompute(int instanceCount) {
		compute.bind();
		buffers.bindAll();

		var groupCount = (instanceCount + 31) >> 5; // ceil(instanceCount / 32)
		glDispatchCompute(groupCount, 1, 1);
	}

	private void uploadInstanceData() {
		long objectPtr = buffers.objectPtr;
		long batchIDPtr = buffers.batchPtr;
		int baseInstance = 0;
		int batchID = 0;
		for (var batch : batches) {
			batch.baseInstance = baseInstance;
			var instancer = batch.instancer;
			for (T t : instancer.getAll()) {
				// write object
				storageBufferWriter.write(objectPtr, t);
				objectPtr += objectStride;

				// write batchID
				MemoryUtil.memPutInt(batchIDPtr, batchID);
				batchIDPtr += IndirectBuffers.INT_SIZE;
			}
			baseInstance += batch.instancer.instanceCount;
			batchID++;
		}

		buffers.flushObjects(objectPtr - buffers.objectPtr);
		buffers.flushBatchIDs(batchIDPtr - buffers.batchPtr);
	}

	private void uploadIndirectCommands() {
		long writePtr = buffers.drawPtr;
		for (Batch<T> batch : batches) {
			batch.writeIndirectCommand(writePtr);
			writePtr += IndirectBuffers.DRAW_COMMAND_STRIDE;
		}
		buffers.flushDrawCommands(writePtr - buffers.drawPtr);
	}

	private int calculateTotalInstanceCount() {
		int total = 0;
		for (Batch<T> batch : batches) {
			batch.instancer.update();
			total += batch.instancer.instanceCount;
		}
		return total;
	}

	private static final class Batch<T extends InstancedPart> {
		final IndirectInstancer<T> instancer;
		final IndirectMeshPool.BufferedMesh mesh;
		final Material material;
		int baseInstance = -1;

		private Batch(IndirectInstancer<T> instancer, Material material, IndirectMeshPool.BufferedMesh mesh) {
			this.instancer = instancer;
			this.material = material;
			this.mesh = mesh;
		}

		public void writeIndirectCommand(long ptr) {
			var boundingSphere = mesh.mesh.getBoundingSphere();

			MemoryUtil.memPutInt(ptr, mesh.getIndexCount()); // count
			MemoryUtil.memPutInt(ptr + 4, 0); // instanceCount - to be incremented by the compute shader
			MemoryUtil.memPutInt(ptr + 8, 0); // firstIndex - all models share the same index buffer
			MemoryUtil.memPutInt(ptr + 12, mesh.getBaseVertex()); // baseVertex
			MemoryUtil.memPutInt(ptr + 16, baseInstance); // baseInstance

			boundingSphere.getToAddress(ptr + 20); // boundingSphere
		}
	}
}
