package com.jozufozu.flywheel.backend.instancing.indirect;

import static org.lwjgl.opengl.GL46.*;

import java.text.Format;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL46C;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.struct.StorageBufferWriter;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.backend.gl.GlNumericType;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.core.Components;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.QuadConverter;
import com.jozufozu.flywheel.core.model.Mesh;
import com.jozufozu.flywheel.core.uniform.UniformBuffer;
import com.jozufozu.flywheel.core.vertex.Formats;

public class IndirectList<T extends InstancedPart> {

	private static final int DRAW_COMMAND_STRIDE = 20;

	final StorageBufferWriter<T> storageBufferWriter;
	final GlProgram compute;
	final GlProgram draw;
	private final StructType<T> type;
	private final long maxObjectCount;
	private final long objectStride;
	private final int maxBatchCount;
	private final long objectClientStorage;
	private final int elementBuffer;

	/**
	 * Stores raw instance data per-object.
	 */
	int objectBuffer;

	int targetBuffer;
	/**
	 * Stores bounding spheres
	 */
	int boundingSphereBuffer;

	/**
	 * Stores drawIndirect structs.
	 */
	int drawBuffer;

	final IndirectMeshPool meshPool;

	int vertexArray;

	final int[] shaderStorageBuffers = new int[4];

	final List<Batch<T>> batches = new ArrayList<>();

	IndirectList(StructType<T> structType) {
		type = structType;
		storageBufferWriter = type.getStorageBufferWriter();

		if (storageBufferWriter == null) {
			throw new NullPointerException();
		}

		glCreateBuffers(shaderStorageBuffers);
		objectBuffer = shaderStorageBuffers[0];
		targetBuffer = shaderStorageBuffers[1];
		boundingSphereBuffer = shaderStorageBuffers[2];
		drawBuffer = shaderStorageBuffers[3];
		meshPool = new IndirectMeshPool(Formats.BLOCK, 1024);

		// FIXME: Resizable buffers
		maxObjectCount = 1024L * 100L;
		maxBatchCount = 64;

		// +4 for the batch id
		objectStride = storageBufferWriter.getAlignment();
		glNamedBufferStorage(objectBuffer, objectStride * maxObjectCount, GL_DYNAMIC_STORAGE_BIT);
		glNamedBufferStorage(targetBuffer, 4 * maxObjectCount, GL_DYNAMIC_STORAGE_BIT);
		glNamedBufferStorage(boundingSphereBuffer, 16 * maxBatchCount, GL_DYNAMIC_STORAGE_BIT);
		glNamedBufferStorage(drawBuffer, DRAW_COMMAND_STRIDE * maxBatchCount, GL_DYNAMIC_STORAGE_BIT);

		objectClientStorage = MemoryUtil.nmemAlloc(objectStride * maxObjectCount);

		vertexArray = glCreateVertexArrays();

		elementBuffer = QuadConverter.getInstance()
				.quads2Tris(2048).buffer.handle();
		setupVertexArray();

		compute = ComputeCullerCompiler.INSTANCE.get(Components.Files.CULL_INSTANCES);
		draw = IndirectDrawCompiler.INSTANCE.get(new IndirectDrawCompiler.Program(Components.Files.DRAW_INDIRECT_VERTEX, Components.Files.DRAW_INDIRECT_FRAGMENT));
	}

	private void setupVertexArray() {
		glVertexArrayElementBuffer(vertexArray, elementBuffer);

		var meshLayout = Formats.BLOCK.getLayout();
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

		glEnableVertexArrayAttrib(vertexArray, meshAttribs);
		glVertexArrayVertexBuffer(vertexArray, meshAttribs, targetBuffer, 0, 4);
		glVertexArrayAttribIFormat(vertexArray, meshAttribs, 1, GlNumericType.UINT.getGlEnum(), 0);
	}

	public void add(Mesh mesh, IndirectInstancer<T> instancer) {
		batches.add(new Batch<>(instancer, meshPool.alloc(mesh)));
	}

	void submit() {
		int instanceCountThisFrame = calculateTotalInstanceCount();

		if (instanceCountThisFrame == 0) {
			return;
		}

		meshPool.uploadAll();
		uploadInstanceData();
		uploadBoundingSpheres();
		uploadIndirectCommands();

		UniformBuffer.getInstance().sync();

		dispatchCompute(instanceCountThisFrame);
		issueMemoryBarrier();
		dispatchDraw();
	}

	private void dispatchDraw() {
		draw.bind();
		Materials.BELL.setup();
		glVertexArrayElementBuffer(vertexArray, elementBuffer);
		glBindVertexArray(vertexArray);
		glBindBuffer(GL_DRAW_INDIRECT_BUFFER, drawBuffer);
		glMultiDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_INT, 0, batches.size(), 0);
		Materials.BELL.clear();
	}

	private static void issueMemoryBarrier() {
		glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);
	}

	private void uploadBoundingSpheres() {
		try (var stack = MemoryStack.stackPush()) {
			final int size = batches.size() * 16;
			final long basePtr = stack.nmalloc(size);

			long ptr = basePtr;
			for (Batch<T> batch : batches) {
				var boundingSphere = batch.mesh.mesh.getBoundingSphere();
				boundingSphere.getToAddress(ptr);
				ptr += 16;
			}

			GL46C.nglNamedBufferSubData(boundingSphereBuffer, 0, size, basePtr);
		}
	}

	private void dispatchCompute(int instanceCount) {
		compute.bind();
		glBindBuffersBase(GL_SHADER_STORAGE_BUFFER, 0, shaderStorageBuffers);

		var groupCount = (instanceCount + 31) >> 5; // ceil(totalInstanceCount / 32)
		glDispatchCompute(groupCount, 1, 1);
	}

	private void uploadInstanceData() {
		long ptr = objectClientStorage;
		int baseInstance = 0;
		int batchID = 0;
		for (var batch : batches) {
			batch.baseInstance = baseInstance;
			var instancer = batch.instancer;
			for (T t : instancer.getAll()) {
				storageBufferWriter.write(ptr, t, batchID);
				ptr += objectStride;
			}
			baseInstance += batch.instancer.instanceCount;
			batchID++;
		}

		GL46C.nglNamedBufferSubData(objectBuffer, 0, ptr - objectClientStorage, objectClientStorage);

	}

	private void uploadIndirectCommands() {
		try (var stack = MemoryStack.stackPush()) {
			var size = batches.size() * DRAW_COMMAND_STRIDE;
			long basePtr = stack.nmalloc(size);
			long writePtr = basePtr;
			for (Batch<T> batch : batches) {
				batch.writeIndirectCommand(writePtr);
				writePtr += DRAW_COMMAND_STRIDE;
			}
			GL46C.nglNamedBufferSubData(drawBuffer, 0, size, basePtr);
		}
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
		int baseInstance;

		private Batch(IndirectInstancer<T> instancer, IndirectMeshPool.BufferedMesh mesh) {
			this.instancer = instancer;
			this.mesh = mesh;
		}

		public void writeIndirectCommand(long ptr) {
			MemoryUtil.memPutInt(ptr, mesh.getVertexCount()); // count
			MemoryUtil.memPutInt(ptr + 4, 0); // instanceCount - to be incremented by the compute shader
			MemoryUtil.memPutInt(ptr + 8, 0); // firstIndex - all models share the same index buffer
			MemoryUtil.memPutInt(ptr + 12, mesh.getBaseVertex()); // baseVertex
			MemoryUtil.memPutInt(ptr + 16, baseInstance); // baseInstance
		}
	}
}
