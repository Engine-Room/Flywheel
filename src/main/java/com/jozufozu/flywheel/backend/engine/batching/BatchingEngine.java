package com.jozufozu.flywheel.backend.engine.batching;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.model.Mesh;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.jozufozu.flywheel.backend.engine.AbstractEngine;
import com.jozufozu.flywheel.backend.engine.AbstractInstancer;
import com.jozufozu.flywheel.backend.engine.InstancerKey;
import com.jozufozu.flywheel.backend.engine.InstancerStorage;
import com.jozufozu.flywheel.lib.task.Flag;
import com.jozufozu.flywheel.lib.task.NamedFlag;
import com.jozufozu.flywheel.lib.task.SimplyComposedPlan;
import com.jozufozu.flywheel.lib.task.Synchronizer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderType;

public class BatchingEngine extends AbstractEngine implements SimplyComposedPlan<RenderContext> {
	private final BatchedDrawTracker drawTracker = new BatchedDrawTracker();

	// TODO: reintroduce BatchedDrawManager
	private final InstancerStorage<BatchedInstancer<?>> storage = new InstancerStorage<>() {
        @Override
        protected <I extends Instance> BatchedInstancer<?> create(InstanceType<I> type) {
            return new BatchedInstancer<>(type);
        }

        @Override
		protected <I extends Instance> void add(InstancerKey<I> key, BatchedInstancer<?> instancer, Model model, RenderStage stage) {
            var stagePlan = stagePlans.computeIfAbsent(stage, renderStage -> new BatchedStagePlan(renderStage, drawTracker));
            var meshes = model.getMeshes();
            for (var entry : meshes.entrySet()) {
                var material = entry.getKey();
                RenderType renderType = material.getFallbackRenderType();
                var transformCall = new TransformCall<>(instancer, material, alloc(entry.getValue(), renderType.format()));
                stagePlan.put(renderType, transformCall);
            }
        }
    };

	private final Map<RenderStage, BatchedStagePlan> stagePlans = new EnumMap<>(RenderStage.class);
	private final Map<VertexFormat, BatchedMeshPool> meshPools = new HashMap<>();

	private final Flag flushFlag = new NamedFlag("flushed");

	public BatchingEngine(int maxOriginDistance) {
		super(maxOriginDistance);
	}

	@Override
	public void execute(TaskExecutor taskExecutor, RenderContext context, Runnable onCompletion) {
		flush();

		// Now it's safe to read stage plans in #renderStage.
		flushFlag.raise();

		BatchContext ctx = BatchContext.create(context, renderOrigin);

		var sync = new Synchronizer(stagePlans.values()
				.size(), onCompletion);

		for (var stagePlan : stagePlans.values()) {
			stagePlan.execute(taskExecutor, ctx, sync);
		}
	}

	@Override
	public Plan<RenderContext> createFramePlan() {
		return this;
	}

	@Override
	public void renderStage(TaskExecutor executor, RenderContext context, RenderStage stage) {
		executor.syncUntil(flushFlag::isRaised);
		if (stage.isLast()) {
			flushFlag.lower();
		}

		var stagePlan = stagePlans.get(stage);

		if (stagePlan == null) {
			drawTracker.draw(stage);
			return;
		}

		executor.syncUntil(stagePlan.flag::isRaised);
		stagePlan.flag.lower();
		drawTracker.draw(stage);
	}

	@Override
	public void renderCrumblingInstances(TaskExecutor executor, RenderContext context, List<Instance> instances, int progress) {
		// TODO: implement
	}

	@Override
	protected InstancerStorage<? extends AbstractInstancer<?>> getStorage() {
		return storage;
	}

	@Override
	public void delete() {
		storage.invalidate();

		meshPools.values()
				.forEach(BatchedMeshPool::delete);
		meshPools.clear();
	}

	private void flush() {
		storage.flush();

		for (var pool : meshPools.values()) {
			pool.flush();
		}
	}

	private BatchedMeshPool.BufferedMesh alloc(Mesh mesh, VertexFormat format) {
		return meshPools.computeIfAbsent(format, BatchedMeshPool::new)
				.alloc(mesh);
	}
}
