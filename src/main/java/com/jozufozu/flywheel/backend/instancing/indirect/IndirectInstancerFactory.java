package com.jozufozu.flywheel.backend.instancing.indirect;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.instancer.Instancer;
import com.jozufozu.flywheel.api.instancer.InstancerFactory;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.core.model.Model;

public class IndirectInstancerFactory<D extends InstancedPart> implements InstancerFactory<D> {

	protected final StructType<D> type;
	private final BiConsumer<IndirectInstancer<?>, Model> creationListener;
	protected final Map<Model, IndirectInstancer<D>> models = new HashMap<>();

	public IndirectInstancerFactory(StructType<D> type, BiConsumer<IndirectInstancer<?>, Model> creationListener) {
		this.type = type;
		this.creationListener = creationListener;
	}

	@Override
	public Instancer<D> model(Model modelKey) {
		return models.computeIfAbsent(modelKey, this::createInstancer);
	}

	private IndirectInstancer<D> createInstancer(Model model) {
		var instancer = new IndirectInstancer<>(type);
		creationListener.accept(instancer, model);
		return instancer;
	}
}
