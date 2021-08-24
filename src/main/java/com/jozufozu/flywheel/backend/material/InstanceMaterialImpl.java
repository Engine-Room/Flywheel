package com.jozufozu.flywheel.backend.material;

import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.jozufozu.flywheel.backend.RenderWork;
import com.jozufozu.flywheel.backend.instancing.InstanceData;
import com.jozufozu.flywheel.backend.instancing.Instancer;
import com.jozufozu.flywheel.backend.model.ModelPool;
import com.jozufozu.flywheel.core.model.IModel;

/**
 * A collection of Instancers that all have the same format.
 * @param <D>
 */
public class InstanceMaterialImpl<D extends InstanceData> implements InstanceMaterial<D> {

	final ModelPool modelPool;
	protected final Cache<Object, Instancer<D>> models;
	protected final MaterialSpec<D> spec;

	public InstanceMaterialImpl(MaterialSpec<D> spec) {
		this.spec = spec;

		modelPool = new ModelPool(spec.getModelFormat(), spec.getModelFormat().getStride() * 64);
		this.models = CacheBuilder.newBuilder()
				.removalListener(notification -> {
					Instancer<?> instancer = (Instancer<?>) notification.getValue();
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
	public Instancer<D> model(Object key, Supplier<IModel> modelSupplier) {
		try {
			return models.get(key, () -> new Instancer<>(modelPool, modelSupplier.get(), spec.getInstanceFactory(), spec.getInstanceFormat()));
		} catch (ExecutionException e) {
			throw new RuntimeException("error creating instancer", e);
		}
	}

	public boolean nothingToRender() {
		return models.size() > 0 && models.asMap()
				.values()
				.stream()
				.allMatch(Instancer::isEmpty);
	}

	public void delete() {
		models.invalidateAll();
		modelPool.delete();
	}

	/**
	 * Clear all instance data without freeing resources.
	 */
	public void clear() {
		models.asMap()
				.values()
				.forEach(Instancer::clear);
	}

}
