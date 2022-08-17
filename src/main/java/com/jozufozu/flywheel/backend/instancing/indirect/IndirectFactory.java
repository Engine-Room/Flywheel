package com.jozufozu.flywheel.backend.instancing.indirect;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.instancer.Instancer;
import com.jozufozu.flywheel.api.instancer.InstancerFactory;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.backend.instancing.AbstractInstancer;
import com.jozufozu.flywheel.core.model.Model;

public class IndirectFactory<D extends InstancedPart> implements InstancerFactory<D> {

	protected final Map<Model, IndirectModel<D>> models = new HashMap<>();
	protected final StructType<D> type;
	private final Consumer<IndirectModel<D>> creationListener;

	public IndirectFactory(StructType<D> type, Consumer<IndirectModel<D>> creationListener) {
		this.type = type;
		this.creationListener = creationListener;
	}

	@Override
	public Instancer<D> model(Model modelKey) {
		return models.computeIfAbsent(modelKey, this::createInstancer).getInstancer();
	}

	public void delete() {
		models.values().forEach(IndirectModel::delete);
		models.clear();
	}

	/**
	 * Clear all instance data without freeing resources.
	 */
	public void clear() {
		models.values()
				.stream()
				.map(IndirectModel::getInstancer)
				.forEach(AbstractInstancer::clear);
	}

	private IndirectModel<D> createInstancer(Model model) {
		var instancer = new IndirectModel<>(type, model);
		this.creationListener.accept(instancer);
		return instancer;
	}
}
