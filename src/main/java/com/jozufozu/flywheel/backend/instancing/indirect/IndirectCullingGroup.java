package com.jozufozu.flywheel.backend.instancing.indirect;

import static org.lwjgl.opengl.GL42.GL_COMMAND_BARRIER_BIT;
import static org.lwjgl.opengl.GL42.glMemoryBarrier;
import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BARRIER_BIT;
import static org.lwjgl.opengl.GL46.glBindVertexArray;
import static org.lwjgl.opengl.GL46.glCreateVertexArrays;
import static org.lwjgl.opengl.GL46.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL46.glDispatchCompute;
import static org.lwjgl.opengl.GL46.glEnableVertexArrayAttrib;
import static org.lwjgl.opengl.GL46.glVertexArrayElementBuffer;
import static org.lwjgl.opengl.GL46.glVertexArrayVertexBuffer;

import com.jozufozu.flywheel.api.RenderStage;
import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.struct.StorageBufferWriter;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.backend.instancing.PipelineCompiler;
import com.jozufozu.flywheel.core.Components;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.QuadConverter;
import com.jozufozu.flywheel.core.uniform.UniformBuffer;

public class IndirectCullingGroup<T extends InstancedPart> {

	private static final int BARRIER_BITS = GL_SHADER_STORAGE_BARRIER_BIT | GL_COMMAND_BARRIER_BIT;

	final StorageBufferWriter<T> storageBufferWriter;
	final GlProgram compute;
	final GlProgram draw;
	private final VertexType vertexType;
	private final long objectStride;

	final IndirectBuffers buffers;

	final IndirectMeshPool meshPool;
	private final int elementBuffer;

	int vertexArray;

	final IndirectDrawSet<T> drawSet = new IndirectDrawSet<>();

	private boolean hasCulledThisFrame;
	private boolean needsMemoryBarrier;
	private int instanceCountThisFrame;

	IndirectCullingGroup(StructType<T> structType, VertexType vertexType) {
		this.vertexType = vertexType;
		storageBufferWriter = structType.getStorageBufferWriter();

		objectStride = storageBufferWriter.getAlignment();
		buffers = new IndirectBuffers(objectStride);
		buffers.createBuffers();
		buffers.createObjectStorage(128);
		buffers.createDrawStorage(2);

		meshPool = new IndirectMeshPool(vertexType, 1024);

		vertexArray = glCreateVertexArrays();

		elementBuffer = QuadConverter.getInstance()
				.quads2Tris(2048).buffer.handle();
		setupVertexArray();

		var indirectShader = structType.getIndirectShader();
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
			offset += attribute.getByteWidth();
		}
	}

	void beginFrame() {
		hasCulledThisFrame = false;
		needsMemoryBarrier = true;
		instanceCountThisFrame = calculateTotalInstanceCountAndPrepareBatches();
	}

	void submit(RenderStage stage) {
		if (drawSet.isEmpty()) {
			return;
		}

		if (instanceCountThisFrame == 0) {
			return;
		}

		cull();
		dispatchDraw(stage);
	}

	private void cull() {
		if (hasCulledThisFrame) {
			return;
		}

		buffers.updateCounts(instanceCountThisFrame, drawSet.size());
		meshPool.uploadAll();
		uploadInstanceData();
		uploadIndirectCommands();

		UniformBuffer.getInstance()
				.sync();

		compute.bind();
		buffers.bindAll();

		var groupCount = (instanceCountThisFrame + 31) >> 5; // ceil(instanceCount / 32)
		glDispatchCompute(groupCount, 1, 1);
		hasCulledThisFrame = true;
	}

	private void dispatchDraw(RenderStage stage) {
		if (!drawSet.contains(stage)) {
			return;
		}

		draw.bind();
		glBindVertexArray(vertexArray);
		buffers.bindObjectAndTarget();
		buffers.bindIndirectBuffer();

		UniformBuffer.getInstance()
				.sync();

		memoryBarrier();

		drawSet.submit(stage);
		glBindVertexArray(0);
	}

	private void memoryBarrier() {
		if (needsMemoryBarrier) {
			glMemoryBarrier(BARRIER_BITS);
			needsMemoryBarrier = false;
		}
	}

	private void uploadInstanceData() {
		long objectPtr = buffers.objectPtr;
		long batchIDPtr = buffers.batchPtr;

		for (int i = 0, batchesSize = drawSet.indirectDraws.size(); i < batchesSize; i++) {
			var batch = drawSet.indirectDraws.get(i);
			var instanceCount = batch.instancer.getInstanceCount();
			batch.writeObjects(objectPtr, batchIDPtr, i);

			objectPtr += instanceCount * objectStride;
			batchIDPtr += instanceCount * IndirectBuffers.INT_SIZE;
		}

		buffers.flushObjects(objectPtr - buffers.objectPtr);
		buffers.flushBatchIDs(batchIDPtr - buffers.batchPtr);
	}

	private void uploadIndirectCommands() {
		long writePtr = buffers.drawPtr;
		for (var batch : drawSet.indirectDraws) {
			batch.writeIndirectCommand(writePtr);
			writePtr += IndirectBuffers.DRAW_COMMAND_STRIDE;
		}
		buffers.flushDrawCommands(writePtr - buffers.drawPtr);
	}

	private int calculateTotalInstanceCountAndPrepareBatches() {
		int baseInstance = 0;
		for (var batch : drawSet.indirectDraws) {
			batch.prepare(baseInstance);
			baseInstance += batch.instancer.instanceCount;
		}
		return baseInstance;
	}

	public void delete() {
		glDeleteVertexArrays(vertexArray);
		buffers.delete();
		meshPool.delete();
	}

}
