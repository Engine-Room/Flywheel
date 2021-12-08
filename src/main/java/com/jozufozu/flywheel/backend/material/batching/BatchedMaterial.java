package com.jozufozu.flywheel.backend.material.batching;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.jozufozu.flywheel.backend.instancing.CPUInstancer;
import com.jozufozu.flywheel.backend.instancing.InstanceData;
import com.jozufozu.flywheel.backend.instancing.Instancer;
import com.jozufozu.flywheel.backend.material.Material;
import com.jozufozu.flywheel.backend.material.MaterialSpec;
import com.jozufozu.flywheel.backend.struct.StructType;
import com.jozufozu.flywheel.core.model.IModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

public class BatchedMaterial<D extends InstanceData> implements Material<D> {

	protected final Map<Object, CPUInstancer<D>> models;
	private final StructType<D> type;

	public BatchedMaterial(MaterialSpec<D> spec) {
		type = spec.getInstanceType();

		this.models = new HashMap<>();
	}

	@Override
	public Instancer<D> model(Object key, Supplier<IModel> modelSupplier) {
		return models.computeIfAbsent(key, $ -> new CPUInstancer<>(type, modelSupplier.get()));
	}

	public void render(PoseStack stack, VertexConsumer buffer) {
		for (CPUInstancer<D> instancer : models.values()) {
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
}
