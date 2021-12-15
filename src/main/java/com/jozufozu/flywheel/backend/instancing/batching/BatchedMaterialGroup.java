package com.jozufozu.flywheel.backend.instancing.batching;

import java.util.HashMap;
import java.util.Map;

import com.jozufozu.flywheel.api.InstanceData;
import com.jozufozu.flywheel.api.MaterialGroup;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.backend.model.DirectBufferBuilder;
import com.jozufozu.flywheel.backend.model.DirectVertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public class BatchedMaterialGroup implements MaterialGroup {

	protected final RenderType state;

	private final Map<StructType<? extends InstanceData>, BatchedMaterial<?>> materials = new HashMap<>();

	public BatchedMaterialGroup(RenderType state) {
		this.state = state;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <D extends InstanceData> BatchedMaterial<D> material(StructType<D> spec) {
		return (BatchedMaterial<D>) materials.computeIfAbsent(spec, BatchedMaterial::new);
	}

	public void render(PoseStack stack, MultiBufferSource source) {
		VertexConsumer buffer = source.getBuffer(state);

		if (buffer instanceof DirectBufferBuilder direct) {
			DirectVertexConsumer consumer = direct.intoDirectConsumer(calculateNeededVertices());

			renderInto(stack, consumer, new FormatContext(consumer.hasOverlay()));

			direct.updateAfterWriting(consumer);
		} else {
			renderInto(stack, buffer, FormatContext.defaultContext());
		}
	}

	private int calculateNeededVertices() {
		int total = 0;
		for (BatchedMaterial<?> material : materials.values()) {
			for (CPUInstancer<?> instancer : material.models.values()) {
				total += instancer.getModelVertexCount() * instancer.numInstances();
			}
		}

		return total;
	}

	private void renderInto(PoseStack stack, VertexConsumer consumer, FormatContext context) {
		for (BatchedMaterial<?> value : materials.values()) {
			value.render(stack, consumer, context);
		}
	}

	public void clear() {
		materials.values().forEach(BatchedMaterial::clear);
	}

	public void delete() {
		materials.clear();
	}
}
