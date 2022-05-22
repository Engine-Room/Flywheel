package com.jozufozu.flywheel.backend.instancing.batching;

import java.util.HashMap;
import java.util.Map;

import com.jozufozu.flywheel.api.InstanceData;
import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.InstancerFactory;
import com.jozufozu.flywheel.api.struct.BatchedStructType;
import com.jozufozu.flywheel.core.model.ModelSupplier;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

public class CPUInstancerFactory<D extends InstanceData> implements InstancerFactory<D> {

	protected final Map<ModelSupplier, CPUInstancer<D>> models;
	private final BatchedStructType<D> type;

	public CPUInstancerFactory(BatchedStructType<D> type) {
		this.type = type;

		this.models = new HashMap<>();
	}

	@Override
	public Instancer<D> model(ModelSupplier modelKey) {
		return models.computeIfAbsent(modelKey, k -> new CPUInstancer<>(type));
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
}
