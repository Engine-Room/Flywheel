package com.jozufozu.flywheel.backend.material;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.jozufozu.flywheel.backend.RenderWork;
import com.jozufozu.flywheel.backend.instancing.InstanceData;
import com.jozufozu.flywheel.backend.instancing.Instancer;
import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.core.model.BlockModel;
import com.jozufozu.flywheel.core.model.IModel;
import com.jozufozu.flywheel.util.Pair;
import com.jozufozu.flywheel.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A collection of Instancers that all have the same format.
 * @param <D>
 */
public class InstanceMaterial<D extends InstanceData> {

	protected final Cache<Object, Instancer<D>> models;
	protected final MaterialSpec<D> spec;

	public InstanceMaterial(MaterialSpec<D> spec) {
		this.spec = spec;

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
	public Instancer<D> model(Object key, Supplier<IModel> modelSupplier) {
		try {
			return models.get(key, () -> new Instancer<>(modelSupplier, spec));
		} catch (ExecutionException e) {
			throw new RuntimeException("error creating instancer", e);
		}
	}

	public Instancer<D> getModel(PartialModel partial, BlockState referenceState) {
		return model(partial, () -> new BlockModel(spec.getModelFormat(), partial.get(), referenceState));
	}

	public Instancer<D> getModel(PartialModel partial, BlockState referenceState, Direction dir) {
		return getModel(partial, referenceState, dir, RenderUtil.rotateToFace(dir));
	}

	public Instancer<D> getModel(PartialModel partial, BlockState referenceState, Direction dir, Supplier<PoseStack> modelTransform) {
		return model(Pair.of(dir, partial), () -> new BlockModel(spec.getModelFormat(), partial.get(), referenceState, modelTransform.get()));
	}

	public Instancer<D> getModel(BlockState toRender) {
		return model(toRender, () -> new BlockModel(spec.getModelFormat(), toRender));
	}

	public boolean nothingToRender() {
		return models.size() > 0 && models.asMap()
				.values()
				.stream()
				.allMatch(Instancer::isEmpty);
	}

	public void delete() {
		models.invalidateAll();
	}

	/**
	 * Clear all instance data without freeing resources.
	 */
	public void clear() {
		models.asMap()
				.values()
				.forEach(Instancer::clear);
	}

	public void forEachInstancer(Consumer<Instancer<D>> f) {
		for (Instancer<D> model : models.asMap()
				.values()) {
			f.accept(model);
		}
	}

}
