package com.jozufozu.flywheel.backend.engine.batching;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.instance.Instancer;
import com.jozufozu.flywheel.api.model.Mesh;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.jozufozu.flywheel.backend.engine.AbstractEngine;
import com.jozufozu.flywheel.backend.engine.InstancerKey;
import com.jozufozu.flywheel.lib.task.SimplyComposedPlan;
import com.jozufozu.flywheel.lib.task.Synchronizer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderType;

public class BatchingEngine extends AbstractEngine implements SimplyComposedPlan<RenderContext> {
	private final BatchedDrawTracker drawTracker = new BatchedDrawTracker();
	private final Map<InstancerKey<?>, BatchedInstancer<?>> instancers = new HashMap<>();
	private final List<UninitializedInstancer> uninitializedInstancers = new ArrayList<>();
	private final List<BatchedInstancer<?>> initializedInstancers = new ArrayList<>();
	private final Map<RenderStage, BatchedStagePlan> stagePlans = new EnumMap<>(RenderStage.class);
	private final Map<VertexFormat, BatchedMeshPool> meshPools = new HashMap<>();

	public BatchingEngine(int maxOriginDistance) {
		super(maxOriginDistance);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <I extends Instance> Instancer<I> instancer(InstanceType<I> type, Model model, RenderStage stage) {
		InstancerKey<I> key = new InstancerKey<>(type, model, stage);
		BatchedInstancer<I> instancer = (BatchedInstancer<I>) instancers.get(key);
		if (instancer == null) {
			instancer = new BatchedInstancer<>(type);
			instancers.put(key, instancer);
			uninitializedInstancers.add(new UninitializedInstancer(instancer, model, stage));
		}
		return instancer;
	}

	@Override
	public void execute(TaskExecutor taskExecutor, RenderContext context, Runnable onCompletion) {
		flush();

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
		executor.syncPoint();
		drawTracker.draw(stage);
	}

	@Override
	protected void onRenderOriginChanged() {
		initializedInstancers.forEach(BatchedInstancer::clear);
	}

	@Override
	public void delete() {
		instancers.clear();

		meshPools.values()
				.forEach(BatchedMeshPool::delete);
		meshPools.clear();

		initializedInstancers.clear();
	}

	@Override
	public void addDebugInfo(List<String> info) {
		info.add("Batching");
		info.add("Origin: " + renderOrigin.getX() + ", " + renderOrigin.getY() + ", " + renderOrigin.getZ());
	}

	private void flush() {
		for (var instancer : uninitializedInstancers) {
			add(instancer.instancer(), instancer.model(), instancer.stage());
		}
		uninitializedInstancers.clear();

		for (var pool : meshPools.values()) {
			pool.flush();
		}
	}

	private void add(BatchedInstancer<?> instancer, Model model, RenderStage stage) {
		var stagePlan = stagePlans.computeIfAbsent(stage, renderStage -> new BatchedStagePlan(renderStage, drawTracker));
		var meshes = model.getMeshes();
		for (var entry : meshes.entrySet()) {
			var material = entry.getKey();
			RenderType renderType = material.getBatchingRenderType();
			var transformCall = new TransformCall<>(instancer, material, alloc(entry.getValue(), renderType.format()));
			stagePlan.put(renderType, transformCall);
		}
		initializedInstancers.add(instancer);
	}

	private BatchedMeshPool.BufferedMesh alloc(Mesh mesh, VertexFormat format) {
		return meshPools.computeIfAbsent(format, BatchedMeshPool::new)
				.alloc(mesh);
	}

	private record UninitializedInstancer(BatchedInstancer<?> instancer, Model model, RenderStage stage) {
	}
}
