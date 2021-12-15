package com.jozufozu.flywheel.backend.instancing.batching;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.jozufozu.flywheel.api.InstanceData;
import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.Material;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.core.model.Model;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

public class BatchedMaterial<D extends InstanceData> implements Material<D> {

	protected final Map<Object, CPUInstancer<D>> models;
	private final StructType<D> type;

	public BatchedMaterial(StructType<D> type) {
		this.type = type;

		this.models = new HashMap<>();
	}

	@Override
	public Instancer<D> model(Object key, Supplier<Model> modelSupplier) {
		return models.computeIfAbsent(key, $ -> new CPUInstancer<>(type, modelSupplier.get()));
	}

	public void render(PoseStack stack, VertexConsumer buffer, FormatContext context) {
		for (CPUInstancer<D> instancer : models.values()) {
			instancer.drawAll(stack, buffer, context);
		}
	}

	/**
	 * Clear all instance data without freeing resources.
	 */
	public void clear() {
		models.values()
				.forEach(CPUInstancer::clear);
	}
}
