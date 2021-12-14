package com.jozufozu.flywheel.backend.instancing.instancing;

import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.RenderWork;
import com.jozufozu.flywheel.api.InstanceData;
import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.Material;
import com.jozufozu.flywheel.backend.model.ImmediateAllocator;
import com.jozufozu.flywheel.backend.model.ModelAllocator;
import com.jozufozu.flywheel.backend.model.ModelPool;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.core.Formats;
import com.jozufozu.flywheel.core.model.Model;

/**
 * A collection of Instancers that all have the same format.
 * @param <D>
 */
public class InstancedMaterial<D extends InstanceData> implements Material<D> {

	final ModelAllocator allocator;
	protected final Cache<Object, GPUInstancer<D>> models;
	protected final StructType<D> type;

	public InstancedMaterial(StructType<D> spec) {
		this.type = spec;

		if (Backend.getInstance().compat.onAMDWindows()) {
			allocator = ImmediateAllocator.INSTANCE;
		} else {
			allocator = new ModelPool(Formats.UNLIT_MODEL, 64);
		}
		this.models = CacheBuilder.newBuilder()
				.removalListener(notification -> {
					GPUInstancer<?> instancer = (GPUInstancer<?>) notification.getValue();
					RenderWork.enqueue(instancer::delete);
				})
				.build();
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
		try {
			return models.get(key, () -> new GPUInstancer<>(type, modelSupplier.get(), allocator));
		} catch (ExecutionException e) {
			throw new RuntimeException("error creating instancer", e);
		}
	}

	public boolean nothingToRender() {
		return models.size() > 0 && models.asMap()
				.values()
				.stream()
				.allMatch(GPUInstancer::isEmpty);
	}

	public void delete() {
		models.invalidateAll();
		if (allocator instanceof ModelPool pool) pool.delete();
	}

	/**
	 * Clear all instance data without freeing resources.
	 */
	public void clear() {
		models.asMap()
				.values()
				.forEach(GPUInstancer::clear);
	}

}
