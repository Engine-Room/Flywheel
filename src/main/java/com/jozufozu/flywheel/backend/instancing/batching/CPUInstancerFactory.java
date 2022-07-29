package com.jozufozu.flywheel.backend.instancing.batching;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.instancer.Instancer;
import com.jozufozu.flywheel.api.instancer.InstancerFactory;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.core.model.Model;

public class CPUInstancerFactory<D extends InstancedPart> implements InstancerFactory<D> {

	protected final Map<Model, BatchedModel<D>> models;
	private final StructType<D> type;
	private final Consumer<BatchedModel<D>> creationListener;

	public CPUInstancerFactory(StructType<D> type, Consumer<BatchedModel<D>> creationListener) {
		this.type = type;

		this.creationListener = creationListener;

		this.models = new HashMap<>();
	}

	@Override
	public Instancer<D> model(Model modelKey) {
		return models.computeIfAbsent(modelKey, this::createModel).getInstancer();
	}

	/**
	 * Clear all instance data without freeing resources.
	 */
	public void clear() {
		models.values()
				.forEach(BatchedModel::clear);
	}

	private BatchedModel<D> createModel(Model k) {
		var out = new BatchedModel<>(type, k);
		creationListener.accept(out);
		return out;
	}
}
