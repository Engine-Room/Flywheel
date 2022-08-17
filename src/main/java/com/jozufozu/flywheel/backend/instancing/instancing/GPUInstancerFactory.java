package com.jozufozu.flywheel.backend.instancing.instancing;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.instancer.Instancer;
import com.jozufozu.flywheel.api.instancer.InstancerFactory;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.core.model.Model;

/**
 * A collection of Instancers that all have the same format.
 * @param <D>
 */
public class GPUInstancerFactory<D extends InstancedPart> implements InstancerFactory<D> {

	protected final Map<Model, GPUInstancer<D>> models = new HashMap<>();
	protected final StructType<D> type;
	private final BiConsumer<GPUInstancer<?>, Model> creationListener;

	public GPUInstancerFactory(StructType<D> type, BiConsumer<GPUInstancer<?>, Model> creationListener) {
		this.type = type;
		this.creationListener = creationListener;
	}

	@Override
	public Instancer<D> model(Model modelKey) {
		return models.computeIfAbsent(modelKey, this::createInstancer);
	}

	private GPUInstancer<D> createInstancer(Model model) {
		var instancer = new GPUInstancer<>(type);
		this.creationListener.accept(instancer, model);
		return instancer;
	}
}
