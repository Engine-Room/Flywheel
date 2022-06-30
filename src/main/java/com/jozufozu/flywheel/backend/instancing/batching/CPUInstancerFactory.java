package com.jozufozu.flywheel.backend.instancing.batching;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.jozufozu.flywheel.api.InstancedPart;
import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.InstancerFactory;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.core.model.ModelSupplier;

public class CPUInstancerFactory<D extends InstancedPart> implements InstancerFactory<D> {

	protected final Map<ModelSupplier, BatchedModel<D>> models;
	private final StructType<D> type;
	private final Consumer<BatchedModel<D>> creationListener;

	public CPUInstancerFactory(StructType<D> type, Consumer<BatchedModel<D>> creationListener) {
		this.type = type;

		this.creationListener = creationListener;

		this.models = new HashMap<>();
	}

	@Override
	public Instancer<D> model(ModelSupplier modelKey) {
		return models.computeIfAbsent(modelKey, this::createModel).instancer;
	}

	/**
	 * Clear all instance data without freeing resources.
	 */
	public void clear() {
		models.values()
				.forEach(BatchedModel::clear);
	}

	private BatchedModel<D> createModel(ModelSupplier k) {
		var out = new BatchedModel<>(type, k);
		creationListener.accept(out);
		return out;
	}
}
