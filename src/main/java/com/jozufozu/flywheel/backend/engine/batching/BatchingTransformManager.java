package com.jozufozu.flywheel.backend.engine.batching;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.instance.Instancer;
import com.jozufozu.flywheel.api.model.Mesh;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.backend.engine.InstancerKey;
import com.jozufozu.flywheel.lib.task.NestedPlan;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;

public class BatchingTransformManager {
	private final Map<InstancerKey<?>, CPUInstancer<?>> instancers = new HashMap<>();
	private final List<UninitializedInstancer> uninitializedInstancers = new ArrayList<>();
	private final List<CPUInstancer<?>> initializedInstancers = new ArrayList<>();
	private final Map<RenderStage, TransformSet> transformSets = new EnumMap<>(RenderStage.class);
	private final Map<VertexFormat, BatchedMeshPool> meshPools = new HashMap<>();

	public Plan plan(PoseStack.Pose matrices, ClientLevel level, BatchingDrawTracker tracker) {
		flush();
		var plans = new ArrayList<Plan>();

		for (var transformSet : transformSets.values()) {
			plans.add(transformSet.plan(matrices, level, tracker));
		}

		return new NestedPlan(plans);
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

	public void flush() {
		for (var instancer : uninitializedInstancers) {
			add(instancer.instancer(), instancer.model(), instancer.stage());
		}
		uninitializedInstancers.clear();

		for (var pool : meshPools.values()) {
			pool.flush();
		}
	}

	public void delete() {
		instancers.clear();

		meshPools.values()
				.forEach(BatchedMeshPool::delete);
		meshPools.clear();

		initializedInstancers.clear();
	}

	public void clearInstancers() {
		initializedInstancers.forEach(CPUInstancer::clear);
	}

	private void add(CPUInstancer<?> instancer, Model model, RenderStage stage) {
		TransformSet transformSet = transformSets.computeIfAbsent(stage, TransformSet::new);
		var meshes = model.getMeshes();
		for (var entry : meshes.entrySet()) {
			var material = entry.getKey();
			RenderType renderType = material.getBatchingRenderType();
			TransformCall<?> transformCall = new TransformCall<>(instancer, material, alloc(entry.getValue(), renderType.format()));
			transformSet.put(renderType, transformCall);
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
