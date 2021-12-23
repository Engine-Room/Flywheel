package com.jozufozu.flywheel.backend.instancing.batching;

import java.util.HashMap;
import java.util.Map;

import com.jozufozu.flywheel.api.InstanceData;
import com.jozufozu.flywheel.api.MaterialGroup;
import com.jozufozu.flywheel.api.struct.Batched;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.backend.instancing.SuperBufferSource;
import com.jozufozu.flywheel.backend.instancing.TaskEngine;
import com.jozufozu.flywheel.backend.model.DirectVertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.RenderType;

public class BatchedMaterialGroup implements MaterialGroup {

	protected final RenderType state;

	private final Map<Batched<? extends InstanceData>, BatchedMaterial<?>> materials = new HashMap<>();

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

	public void render(PoseStack stack, SuperBufferSource source, TaskEngine pool) {

		int vertexCount = 0;
		for (BatchedMaterial<?> material : materials.values()) {
			for (CPUInstancer<?> instancer : material.models.values()) {
				instancer.setup();
				vertexCount += instancer.getTotalVertexCount();
			}
		}

		DirectVertexConsumer consumer = source.getBuffer(state, vertexCount);

		// avoids rendering garbage, but doesn't fix the issue of some instances not being buffered
		consumer.memSetZero();

		for (BatchedMaterial<?> material : materials.values()) {
			for (CPUInstancer<?> instancer : material.models.values()) {
				if (consumer.hasOverlay()) {
					instancer.sbb.context.fullNormalTransform = false;
					instancer.sbb.context.outputColorDiffuse = false;
				} else {
					instancer.sbb.context.fullNormalTransform = false;
					instancer.sbb.context.outputColorDiffuse = true;
				}
				instancer.submitTasks(stack, pool, consumer);
			}
		}
	}

	public void clear() {
		materials.values().forEach(BatchedMaterial::clear);
	}

	public void delete() {
		materials.clear();
	}
}
