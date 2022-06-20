package com.jozufozu.flywheel.backend.instancing.instancing;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.jozufozu.flywheel.api.InstancedPart;
import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.InstancerFactory;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.core.model.ModelSupplier;

import net.minecraft.client.renderer.RenderType;

/**
 * A collection of Instancers that all have the same format.
 * @param <D>
 */
public class GPUInstancerFactory<D extends InstancedPart> implements InstancerFactory<D> {

	protected final Map<ModelSupplier, InstancedModel<D>> models = new HashMap<>();
	protected final StructType<D> type;
	private final Consumer<InstancedModel<D>> creationListener;

	public GPUInstancerFactory(StructType<D> type, Consumer<InstancedModel<D>> creationListener) {
		this.type = type;
		this.creationListener = creationListener;
	}

	@Override
	public Instancer<D> model(ModelSupplier modelKey) {
		return models.computeIfAbsent(modelKey, this::createInstancer).getInstancer();
	}

	public int getInstanceCount() {
		return models.values()
				.stream()
				.map(InstancedModel::getInstancer)
				.mapToInt(GPUInstancer::getInstanceCount)
				.sum();
	}

	public int getVertexCount() {
		return models.values()
				.stream()
				.mapToInt(InstancedModel::getVertexCount)
				.sum();
	}

	public void delete() {
		models.values().forEach(InstancedModel::delete);
		models.clear();
	}

	/**
	 * Clear all instance data without freeing resources.
	 */
	public void clear() {
		models.values()
				.stream()
				.map(InstancedModel::getInstancer)
				.forEach(GPUInstancer::clear);
	}

	private InstancedModel<D> createInstancer(ModelSupplier model) {
		var instancer = new InstancedModel<>(type, model);
		this.creationListener.accept(instancer);
		return instancer;
	}
}
