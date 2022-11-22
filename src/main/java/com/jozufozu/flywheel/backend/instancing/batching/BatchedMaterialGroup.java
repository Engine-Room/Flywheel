package com.jozufozu.flywheel.backend.instancing.batching;

import java.util.HashMap;
import java.util.Map;

import com.jozufozu.flywheel.api.InstanceData;
import com.jozufozu.flywheel.api.MaterialGroup;
import com.jozufozu.flywheel.api.struct.Batched;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.backend.ShadersModHandler;
import com.jozufozu.flywheel.backend.instancing.BatchDrawingTracker;
import com.jozufozu.flywheel.backend.instancing.TaskEngine;
import com.jozufozu.flywheel.backend.model.DirectVertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.RenderType;

public class BatchedMaterialGroup implements MaterialGroup {

	protected final RenderType state;

	private final Map<Batched<? extends InstanceData>, BatchedMaterial<?>> materials = new HashMap<>();
	private int vertexCount;
	private int instanceCount;

	public BatchedMaterialGroup(RenderType state) {
		this.state = state;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <D extends InstanceData> BatchedMaterial<D> material(StructType<D> type) {
		if (type instanceof Batched<D> batched) {
			return (BatchedMaterial<D>) materials.computeIfAbsent(batched, BatchedMaterial::new);
		} else {
			throw new ClassCastException("Cannot use type '" + type + "' with CPU instancing.");
		}
	}

	public void render(PoseStack stack, BatchDrawingTracker source, TaskEngine pool) {

		vertexCount = 0;
		instanceCount = 0;
		for (BatchedMaterial<?> material : materials.values()) {
			for (CPUInstancer<?> instancer : material.models.values()) {
				instancer.setup();
				vertexCount += instancer.getVertexCount();
				instanceCount += instancer.getInstanceCount();
			}
		}

		DirectVertexConsumer consumer = source.getDirectConsumer(state, vertexCount);

		// avoids rendering garbage, but doesn't fix the issue of some instances not being buffered
		consumer.memSetZero();

		for (BatchedMaterial<?> material : materials.values()) {
			for (CPUInstancer<?> instancer : material.models.values()) {
				instancer.sbb.context.outputColorDiffuse = !consumer.hasOverlay() && !ShadersModHandler.isShaderPackInUse();
				instancer.submitTasks(stack, pool, consumer);
			}
		}
	}

	public void clear() {
		materials.values().forEach(BatchedMaterial::clear);
	}

	public void delete() {
		materials.values()
				.forEach(BatchedMaterial::delete);

		materials.clear();
	}

	/**
	 * Get the number of instances drawn last frame.
	 * @return The instance count.
	 */
	public int getInstanceCount() {
		return instanceCount;
	}

	/**
	 * Get the number of vertices drawn last frame.
	 * @return The vertex count.
	 */
	public int getVertexCount() {
		return vertexCount;
	}
}
