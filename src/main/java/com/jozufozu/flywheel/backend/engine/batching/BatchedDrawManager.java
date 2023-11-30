package com.jozufozu.flywheel.backend.engine.batching;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.model.Mesh;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.jozufozu.flywheel.backend.engine.InstancerKey;
import com.jozufozu.flywheel.backend.engine.InstancerStorage;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderType;

class BatchedDrawManager extends InstancerStorage<BatchedInstancer<?>> {
	public final BatchedDrawTracker drawTracker = new BatchedDrawTracker();
	private final Map<RenderStage, BatchedStagePlan> stagePlans = new EnumMap<>(RenderStage.class);
	private final Map<VertexFormat, BatchedMeshPool> meshPools = new HashMap<>();

	public Collection<BatchedStagePlan> getStagePlans() {
		return stagePlans.values();
	}

	public void renderStage(TaskExecutor executor, RenderStage stage) {
		var stagePlan = stagePlans.get(stage);

		if (stagePlan == null) {
			return;
		}

		executor.syncUntil(stagePlan.flag::isRaised);
		stagePlan.flag.lower();

		drawTracker.draw(stage);
	}

	@Override
	protected <I extends Instance> BatchedInstancer<?> create(InstanceType<I> type) {
		return new BatchedInstancer<>(type);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected <I extends Instance> void add(InstancerKey<I> key, BatchedInstancer<?> instancer, Model model, RenderStage stage) {
		var stagePlan = stagePlans.computeIfAbsent(stage, renderStage -> new BatchedStagePlan(renderStage, drawTracker));
		var meshes = model.getMeshes();
		for (var entry : meshes.entrySet()) {
			var material = entry.getKey();
			RenderType renderType = material.getFallbackRenderType();
			var mesh = alloc(entry.getValue(), renderType.format());
			var transformCall = new TransformCall<>(instancer, material, mesh);
			stagePlan.put(renderType, transformCall);
			instancer.addTransformCall((TransformCall) transformCall);
		}
	}

	@Override
	public void flush() {
		super.flush();

		for (var pool : meshPools.values()) {
			pool.flush();
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();

		meshPools.values()
				.forEach(BatchedMeshPool::delete);
		meshPools.clear();
	}

	private BatchedMeshPool.BufferedMesh alloc(Mesh mesh, VertexFormat format) {
		return meshPools.computeIfAbsent(format, BatchedMeshPool::new)
				.alloc(mesh);
	}
}
