package com.jozufozu.flywheel.backend.engine.indirect;

import static org.lwjgl.opengl.GL42.GL_COMMAND_BARRIER_BIT;
import static org.lwjgl.opengl.GL42.glMemoryBarrier;
import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BARRIER_BIT;
import static org.lwjgl.opengl.GL43.glDispatchCompute;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.model.Mesh;
import com.jozufozu.flywheel.backend.compile.IndirectPrograms;
import com.jozufozu.flywheel.backend.engine.UniformBuffer;
import com.jozufozu.flywheel.gl.shader.GlProgram;
import com.jozufozu.flywheel.lib.context.Contexts;

public class IndirectCullingGroup<I extends Instance> {
	private static final int BARRIER_BITS = GL_SHADER_STORAGE_BARRIER_BIT | GL_COMMAND_BARRIER_BIT;

	private final GlProgram compute;
	private final GlProgram draw;
	private final long objectStride;
	private final IndirectBuffers buffers;
	public final IndirectMeshPool meshPool;
	public final IndirectDrawSet<I> drawSet = new IndirectDrawSet<>();
	private boolean hasCulledThisFrame;
	private boolean needsMemoryBarrier;
	private int instanceCountThisFrame;

	IndirectCullingGroup(InstanceType<I> instanceType) {
		objectStride = instanceType.getLayout()
				.getStride() + IndirectBuffers.INT_SIZE;

		buffers = new IndirectBuffers(objectStride);
		buffers.createBuffers();
		buffers.createObjectStorage(128);
		buffers.createDrawStorage(2);

		meshPool = new IndirectMeshPool();

		var indirectPrograms = IndirectPrograms.get();
		compute = indirectPrograms.getCullingProgram(instanceType);
		draw = indirectPrograms.getIndirectProgram(instanceType, Contexts.WORLD);
	}

	public void add(IndirectInstancer<I> instancer, RenderStage stage, Material material, Mesh mesh) {
		drawSet.add(instancer, material, stage, meshPool.alloc(mesh));
	}

	public void beginFrame() {
		hasCulledThisFrame = false;
		needsMemoryBarrier = true;
		instanceCountThisFrame = calculateTotalInstanceCountAndPrepareBatches();
	}

	public void submit(RenderStage stage) {
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
		meshPool.bindForDraw();
		buffers.bindForDraw();

		memoryBarrier();

		drawSet.submit(stage);
	}

	private void memoryBarrier() {
		if (needsMemoryBarrier) {
			glMemoryBarrier(BARRIER_BITS);
			needsMemoryBarrier = false;
		}
	}

	private void uploadInstances() {
		long objectPtr = buffers.objectPtr;

		for (int i = 0, batchesSize = drawSet.indirectDraws.size(); i < batchesSize; i++) {
			var batch = drawSet.indirectDraws.get(i);
			var instanceCount = batch.instancer()
					.getInstanceCount();
			batch.writeObjects(objectPtr, i);

			objectPtr += instanceCount * objectStride;
		}

		buffers.flushObjects(objectPtr - buffers.objectPtr);
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
		buffers.delete();
		meshPool.delete();
	}

	public boolean hasStage(RenderStage stage) {
		return drawSet.contains(stage);
	}
}
