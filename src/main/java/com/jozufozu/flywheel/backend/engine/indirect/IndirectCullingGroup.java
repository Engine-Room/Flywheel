package com.jozufozu.flywheel.backend.engine.indirect;

import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL42.GL_COMMAND_BARRIER_BIT;
import static org.lwjgl.opengl.GL42.glMemoryBarrier;
import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BARRIER_BIT;
import static org.lwjgl.opengl.GL43.glDispatchCompute;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.compile.IndirectPrograms;
import com.jozufozu.flywheel.backend.engine.UniformBuffer;
import com.jozufozu.flywheel.gl.array.GlVertexArray;
import com.jozufozu.flywheel.gl.shader.GlProgram;
import com.jozufozu.flywheel.lib.context.Contexts;
import com.jozufozu.flywheel.lib.util.QuadConverter;

public class IndirectCullingGroup<I extends Instance> {

	private static final int BARRIER_BITS = GL_SHADER_STORAGE_BARRIER_BIT | GL_COMMAND_BARRIER_BIT;

	final GlProgram compute;
	final GlProgram draw;
	private final VertexType vertexType;
	private final long instanceStride;

	final IndirectBuffers buffers;

	final IndirectMeshPool meshPool;
	private final int elementBuffer;

	GlVertexArray vertexArray;

	final IndirectDrawSet<I> drawSet = new IndirectDrawSet<>();

	private boolean hasCulledThisFrame;
	private boolean needsMemoryBarrier;
	private int instanceCountThisFrame;

	IndirectCullingGroup(InstanceType<I> instanceType, VertexType vertexType) {
		this.vertexType = vertexType;

		instanceStride = instanceType.getLayout()
				.getStride();
		buffers = new IndirectBuffers(instanceStride);
		buffers.createBuffers();
		buffers.createObjectStorage(128);
		buffers.createDrawStorage(2);

		meshPool = new IndirectMeshPool(vertexType, 1024);

		vertexArray = new GlVertexArray();

		elementBuffer = QuadConverter.getInstance()
				.quads2Tris(2048).glBuffer;
		setupVertexArray();

		var indirectPrograms = IndirectPrograms.get();
		compute = indirectPrograms.getCullingProgram(instanceType);
		draw = indirectPrograms.getIndirectProgram(vertexType, instanceType, Contexts.WORLD);
	}

	private void setupVertexArray() {
		vertexArray.setElementBuffer(elementBuffer);
		vertexArray.bindAttributes(vertexType.getLayout(), meshPool.vbo, 0, 0);
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
		meshPool.flush();
		uploadInstances();
		uploadIndirectCommands();

		UniformBuffer.syncAndBind(compute);
		buffers.bindForCompute();

		var groupCount = (instanceCountThisFrame + 31) >> 5; // ceil(instanceCount / 32)
		glDispatchCompute(groupCount, 1, 1);
		hasCulledThisFrame = true;
	}

	private void dispatchDraw(RenderStage stage) {
		if (!drawSet.contains(stage)) {
			return;
		}

		UniformBuffer.syncAndBind(draw);
		vertexArray.bindForDraw();
		buffers.bindForDraw();

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

	private void uploadInstances() {
		long objectPtr = buffers.objectPtr;
		long batchIDPtr = buffers.batchPtr;

		for (int i = 0, batchesSize = drawSet.indirectDraws.size(); i < batchesSize; i++) {
			var batch = drawSet.indirectDraws.get(i);
			var instanceCount = batch.instancer()
					.getInstanceCount();
			batch.writeObjects(objectPtr, batchIDPtr, i);

			objectPtr += instanceCount * instanceStride;
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
			baseInstance += batch.instancer().getInstanceCount();
		}
		return baseInstance;
	}

	public void delete() {
		vertexArray.delete();
		buffers.delete();
		meshPool.delete();
	}

	public boolean hasStage(RenderStage stage) {
		return drawSet.contains(stage);
	}
}
