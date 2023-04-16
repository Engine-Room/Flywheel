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
import com.jozufozu.flywheel.lib.task.NestedPlan;
import com.jozufozu.flywheel.util.FlwUtil;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;

public class BatchingEngine extends AbstractEngine {
	private final BatchingDrawTracker drawTracker = new BatchingDrawTracker();
	private final Map<InstancerKey<?>, CPUInstancer<?>> instancers = new HashMap<>();
	private final List<UninitializedInstancer> uninitializedInstancers = new ArrayList<>();
	private final List<CPUInstancer<?>> initializedInstancers = new ArrayList<>();
	private final Map<RenderStage, BatchingStage> stages = new EnumMap<>(RenderStage.class);
	private final Map<VertexFormat, BatchedMeshPool> meshPools = new HashMap<>();

	public BatchingEngine(int maxOriginDistance) {
		super(maxOriginDistance);
	}

	@Override
	public <I extends Instance> Instancer<I> instancer(InstanceType<I> type, Model model, RenderStage stage) {
		return this.getInstancer(type, model, stage);
	}

	@Override
	public Plan planThisFrame(RenderContext context) {
		Vec3 cameraPos = context.camera()
				.getPosition();
		var stack = FlwUtil.copyPoseStack(context.stack());
		stack.translate(renderOrigin.getX() - cameraPos.x, renderOrigin.getY() - cameraPos.y, renderOrigin.getZ() - cameraPos.z);

		flush();

		var plans = new ArrayList<Plan>();

		for (var transformSet : stages.values()) {
			plans.add(transformSet.plan(stack.last(), context.level()));
		}

		return new NestedPlan(plans);
	}

	@Override
	public void renderStage(TaskExecutor executor, RenderContext context, RenderStage stage) {
		if (!drawTracker.hasStage(stage)) {
			return;
		}
		executor.syncPoint();
		drawTracker.draw(stage);
	}

	@Override
	protected void onRenderOriginChanged() {
		initializedInstancers.forEach(CPUInstancer::clear);
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

	@SuppressWarnings("unchecked")
	public <I extends Instance> Instancer<I> getInstancer(InstanceType<I> type, Model model, RenderStage stage) {
		InstancerKey<I> key = new InstancerKey<>(type, model, stage);
		CPUInstancer<I> instancer = (CPUInstancer<I>) instancers.get(key);
		if (instancer == null) {
			instancer = new CPUInstancer<>(type);
			instancers.put(key, instancer);
			uninitializedInstancers.add(new UninitializedInstancer(instancer, model, stage));
		}
		return instancer;
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

	private void add(CPUInstancer<?> instancer, Model model, RenderStage stage) {
		var batchingStage = stages.computeIfAbsent(stage, renderStage -> new BatchingStage(renderStage, drawTracker));
		var meshes = model.getMeshes();
		for (var entry : meshes.entrySet()) {
			var material = entry.getKey();
			RenderType renderType = material.getBatchingRenderType();
			var transformCall = new TransformCall<>(instancer, material, alloc(entry.getValue(), renderType.format()));
			batchingStage.put(renderType, transformCall);
		}
		initializedInstancers.add(instancer);
	}

	private BatchedMeshPool.BufferedMesh alloc(Mesh mesh, VertexFormat format) {
		return meshPools.computeIfAbsent(format, BatchedMeshPool::new)
				.alloc(mesh);
	}

	private record UninitializedInstancer(CPUInstancer<?> instancer, Model model, RenderStage stage) {
	}
}
