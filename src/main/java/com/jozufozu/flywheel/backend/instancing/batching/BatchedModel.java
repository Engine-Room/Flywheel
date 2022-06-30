package com.jozufozu.flywheel.backend.instancing.batching;

import java.util.List;

import com.jozufozu.flywheel.api.InstancedPart;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.core.model.ModelSupplier;

public class BatchedModel<D extends InstancedPart> {

	CPUInstancer<D> instancer;
	ModelSupplier model;
	StructType<D> type;
	private List<TransformSet<D>> layers;

	public BatchedModel(StructType<D> type, ModelSupplier model) {
		this.type = type;
		this.model = model;
		this.instancer = new CPUInstancer<>(type);
	}

	public void init(BatchLists batchLists) {
		layers = model.get()
				.entrySet()
				.stream()
				.map(entry -> new TransformSet<>(instancer, entry.getKey(), entry.getValue()))
				.toList();

		for (TransformSet<D> layer : layers) {
			batchLists.add(layer);
		}
	}

	public void clear() {
		instancer.clear();
	}

}
