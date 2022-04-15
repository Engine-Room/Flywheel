package com.jozufozu.flywheel.backend.instancing.batching;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jozufozu.flywheel.api.InstanceData;
import com.jozufozu.flywheel.api.struct.Batched;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.backend.instancing.BatchDrawingTracker;
import com.jozufozu.flywheel.backend.instancing.Engine;
import com.jozufozu.flywheel.backend.instancing.TaskEngine;
import com.jozufozu.flywheel.core.RenderContext;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

public class BatchingEngine implements Engine {

	private final Map<Batched<? extends InstanceData>, BatchedMaterial<?>> materials = new HashMap<>();
	private final BatchDrawingTracker batchTracker = new BatchDrawingTracker();

	@SuppressWarnings("unchecked")
	@Override
	public <D extends InstanceData> BatchedMaterial<D> material(StructType<D> type) {
		if (type instanceof Batched<D> batched) {
			return (BatchedMaterial<D>) materials.computeIfAbsent(batched, BatchedMaterial::new);
		} else {
			throw new ClassCastException("Cannot use type '" + type + "' with batching.");
		}
	}

	@Override
	public Vec3i getOriginCoordinate() {
		return BlockPos.ZERO;
	}

	@Override
	public void render(TaskEngine taskEngine, RenderContext context) {

//		vertexCount = 0;
//		instanceCount = 0;
//		for (BatchedMaterial<?> material : materials.values()) {
//			for (CPUInstancer<?> instancer : material.models.values()) {
//				instancer.setup();
//				vertexCount += instancer.getVertexCount();
//				instanceCount += instancer.getInstanceCount();
//			}
//		}
//
//		DirectVertexConsumer consumer = batchTracker.getDirectConsumer(state, vertexCount);
//
//		// avoids rendering garbage, but doesn't fix the issue of some instances not being buffered
//		consumer.memSetZero();
//
//		for (BatchedMaterial<?> material : materials.values()) {
//			for (CPUInstancer<?> instancer : material.models.values()) {
//				instancer.sbb.context.outputColorDiffuse = !consumer.hasOverlay() && !OptifineHandler.isUsingShaders();
//				instancer.submitTasks(stack, pool, consumer);
//			}
//		}

		// FIXME: this probably breaks some vanilla stuff but it works much better for flywheel
		Matrix4f mat = new Matrix4f();
		mat.setIdentity();
		if (context.level().effects().constantAmbientLight()) {
			Lighting.setupNetherLevel(mat);
		} else {
			Lighting.setupLevel(mat);
		}

		taskEngine.syncPoint();
		batchTracker.endBatch();
	}

	@Override
	public void delete() {
	}

	@Override
	public void beginFrame(Camera info) {

	}

	@Override
	public void addDebugInfo(List<String> info) {
		info.add("Batching");
		info.add("Instances: " + 0);
		info.add("Vertices: " + 0);
	}
}
