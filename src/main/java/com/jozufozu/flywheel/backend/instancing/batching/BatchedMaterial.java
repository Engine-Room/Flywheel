package com.jozufozu.flywheel.backend.instancing.batching;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.jozufozu.flywheel.api.InstanceData;
import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.Material;
import com.jozufozu.flywheel.api.struct.Batched;
import com.jozufozu.flywheel.core.model.Model;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

public class BatchedMaterial<D extends InstanceData> implements Material<D> {

	protected final Map<Object, CPUInstancer<D>> models;
	private final Batched<D> type;

	public BatchedMaterial(Batched<D> type) {
		this.type = type;

		this.models = new HashMap<>();
	}

	@Override
	public Instancer<D> model(Object key, Supplier<Model> modelSupplier) {
		return models.computeIfAbsent(key, $ -> new CPUInstancer<>(type, modelSupplier.get()));
	}

	public void setupAndRenderInto(PoseStack stack, VertexConsumer buffer) {
		for (CPUInstancer<D> instancer : models.values()) {
			instancer.setup();
			instancer.drawAll(stack, buffer);
		}
	}

	/**
	 * Clear all instance data without freeing resources.
	 */
	public void clear() {
		models.values()
				.forEach(CPUInstancer::clear);
	}

	public void delete() {
		models.values().forEach(CPUInstancer::delete);
		models.clear();
	}
}
