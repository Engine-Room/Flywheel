package com.jozufozu.flywheel.backend.instancing.instancing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.jozufozu.flywheel.api.InstanceData;
import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.Material;
import com.jozufozu.flywheel.api.struct.Instanced;
import com.jozufozu.flywheel.backend.model.ModelAllocator;
import com.jozufozu.flywheel.core.model.Model;

/**
 * A collection of Instancers that all have the same format.
 * @param <D>
 */
public class InstancedMaterial<D extends InstanceData> implements Material<D> {

	protected final ModelAllocator allocator;
	protected final Map<Object, GPUInstancer<D>> models = new HashMap<>();
	protected final Instanced<D> type;
	protected final List<GPUInstancer<D>> uninitialized = new ArrayList<>();

	public InstancedMaterial(Instanced<D> type, ModelAllocator allocator) {
		this.type = type;
		this.allocator = allocator;
	}

	/**
	 * Get an instancer for the given model. Calling this method twice with the same key will return the same instancer.
	 *
	 * @param key An object that uniquely identifies the model.
	 * @param modelSupplier A factory that creates the IModel that you want to render.
	 * @return An instancer for the given model, capable of rendering many copies for little cost.
	 */
	@Override
	public Instancer<D> model(Object key, Supplier<Model> modelSupplier) {
		return models.computeIfAbsent(key, $ -> {
			GPUInstancer<D> instancer = new GPUInstancer<>(type, modelSupplier.get(), allocator);
			uninitialized.add(instancer);
			return instancer;
		});
	}

	public int getInstanceCount() {
		return models.values().stream().mapToInt(GPUInstancer::getInstanceCount).sum();
	}

	public int getVertexCount() {
		return models.values().stream().mapToInt(GPUInstancer::getVertexCount).sum();
	}

	public boolean nothingToRender() {
		return models.size() > 0 && models.values()
				.stream()
				.allMatch(GPUInstancer::isEmpty);
	}

	public void delete() {
		models.values().forEach(GPUInstancer::delete);
		models.clear();
	}

	/**
	 * Clear all instance data without freeing resources.
	 */
	public void clear() {
		models.values()
				.forEach(GPUInstancer::clear);
	}

	public Collection<GPUInstancer<D>> getAllInstancers() {
		return models.values();
	}
}
