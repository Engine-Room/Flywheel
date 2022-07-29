package com.jozufozu.flywheel.backend.instancing.batching;

import java.util.List;

import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.core.model.Model;

public class BatchedModel<D extends InstancedPart> {

	private final StructType<D> type;
	private final Model model;
	private final CPUInstancer<D> instancer;
	private List<TransformSet<D>> layers;

	public BatchedModel(StructType<D> type, Model model) {
		this.type = type;
		this.model = model;
		this.instancer = new CPUInstancer<>(type);
	}

	public void init(BatchLists batchLists) {
		layers = model.getMeshes()
				.entrySet()
				.stream()
				.map(entry -> new TransformSet<>(instancer, entry.getKey(), entry.getValue()))
				.toList();

		for (TransformSet<D> layer : layers) {
			batchLists.add(layer);
		}
	}

	public Model getModel() {
		return model;
	}

	public CPUInstancer<D> getInstancer() {
		return instancer;
	}

	public void clear() {
		instancer.clear();
	}

}
