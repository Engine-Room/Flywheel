package com.jozufozu.flywheel.backend.instancing.batching;

import java.util.HashMap;
import java.util.Map;

import com.jozufozu.flywheel.api.InstanceData;
import com.jozufozu.flywheel.api.MaterialGroup;
import com.jozufozu.flywheel.api.struct.Batched;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.backend.instancing.TaskEngine;
import com.jozufozu.flywheel.backend.model.DirectBufferBuilder;
import com.jozufozu.flywheel.backend.model.DirectVertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
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

	public void render(PoseStack stack, MultiBufferSource source, TaskEngine pool) {
		VertexConsumer buffer = source.getBuffer(state);

		if (buffer instanceof DirectBufferBuilder direct) {
			renderParallel(stack, pool, direct);
		} else {
			renderSerial(stack, buffer);
		}
	}

	private void renderParallel(PoseStack stack, TaskEngine pool, DirectBufferBuilder direct) {
		int vertexCount = 0;
		for (BatchedMaterial<?> material : materials.values()) {
			for (CPUInstancer<?> instancer : material.models.values()) {
				instancer.setup();
				vertexCount += instancer.getTotalVertexCount();
			}
		}

		DirectVertexConsumer consumer = direct.intoDirectConsumer(vertexCount);

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

	private void renderSerial(PoseStack stack, VertexConsumer consumer) {
		for (BatchedMaterial<?> value : materials.values()) {
			value.setupAndRenderInto(stack, consumer);
		}
	}

	public void clear() {
		materials.values().forEach(BatchedMaterial::clear);
	}

	public void delete() {
		materials.clear();
	}
}
