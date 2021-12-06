package com.jozufozu.flywheel.backend.material;

import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.jozufozu.flywheel.backend.RenderWork;
import com.jozufozu.flywheel.backend.instancing.GPUInstancer;
import com.jozufozu.flywheel.backend.instancing.InstanceData;
import com.jozufozu.flywheel.backend.instancing.Instancer;
import com.jozufozu.flywheel.backend.model.ModelPool;
import com.jozufozu.flywheel.backend.struct.StructType;
import com.jozufozu.flywheel.core.model.IModel;

/**
 * A collection of Instancers that all have the same format.
 * @param <D>
 */
public class MaterialImpl<D extends InstanceData> implements Material<D> {

	final ModelPool modelPool;
	protected final Cache<Object, GPUInstancer<D>> models;
	protected final StructType<D> type;

	public MaterialImpl(MaterialSpec<D> spec) {
		this.type = spec.getInstanceType();

		modelPool = new ModelPool(spec.getModelFormat(), 64);
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
	public Instancer<D> model(Object key, Supplier<IModel> modelSupplier) {
		try {
			return models.get(key, () -> new GPUInstancer<>(type, modelSupplier.get(), modelPool));
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
		modelPool.delete();
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
