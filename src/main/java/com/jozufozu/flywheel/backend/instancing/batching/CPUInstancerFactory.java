package com.jozufozu.flywheel.backend.instancing.batching;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.instancer.Instancer;
import com.jozufozu.flywheel.api.instancer.InstancerFactory;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.core.model.Model;

public class CPUInstancerFactory<D extends InstancedPart> implements InstancerFactory<D> {

	protected final StructType<D> type;
	private final BiConsumer<CPUInstancer<?>, Model> creationListener;
	protected final Map<Model, CPUInstancer<D>> models = new HashMap<>();

	public CPUInstancerFactory(StructType<D> type, BiConsumer<CPUInstancer<?>, Model> creationListener) {
		this.type = type;
		this.creationListener = creationListener;
	}

	@Override
	public Instancer<D> model(Model modelKey) {
		return models.computeIfAbsent(modelKey, this::createInstancer);
	}

	private CPUInstancer<D> createInstancer(Model model) {
		var instancer = new CPUInstancer<>(type);
		creationListener.accept(instancer, model);
		return instancer;
	}
}
