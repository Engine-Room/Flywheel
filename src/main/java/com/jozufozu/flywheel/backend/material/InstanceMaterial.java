package com.jozufozu.flywheel.backend.material;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.jozufozu.flywheel.backend.RenderWork;
import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.instancing.InstanceData;
import com.jozufozu.flywheel.backend.instancing.Instancer;
import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.core.model.BlockModel;
import com.jozufozu.flywheel.core.model.IModel;
import com.jozufozu.flywheel.util.RenderUtil;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3i;

public class InstanceMaterial<D extends InstanceData> {

	protected final Supplier<Vector3i> originCoordinate;
	protected final Cache<Object, Instancer<D>> models;
	protected final MaterialSpec<D> spec;
	private final VertexFormat modelFormat;

	public InstanceMaterial(Supplier<Vector3i> renderer, MaterialSpec<D> spec) {
		this.originCoordinate = renderer;
		this.spec = spec;

		this.models = CacheBuilder.newBuilder()
				.removalListener(notification -> {
					Instancer<?> instancer = (Instancer<?>) notification.getValue();
					RenderWork.enqueue(instancer::delete);
				})
				.build();
		modelFormat = this.spec.getModelFormat();
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

	public Instancer<D> getModel(PartialModel partial, BlockState referenceState) {
		return model(partial, () -> buildModel(partial.get(), referenceState));
	}

	public Instancer<D> getModel(PartialModel partial, BlockState referenceState, Direction dir) {
		return getModel(partial, referenceState, dir, RenderUtil.rotateToFace(dir));
	}

	public Instancer<D> getModel(PartialModel partial, BlockState referenceState, Direction dir, Supplier<MatrixStack> modelTransform) {
		return model(Pair.of(dir, partial), () -> buildModel(partial.get(), referenceState, modelTransform.get()));
	}

	public Instancer<D> getModel(BlockState toRender) {
		return model(toRender, () -> buildModel(toRender));
	}

	public Instancer<D> model(Object key, Supplier<IModel> supplier) {
		try {
			return models.get(key, () -> new Instancer<>(supplier, originCoordinate, spec));
		} catch (ExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}

	private IModel buildModel(BlockState renderedState) {
		BlockRendererDispatcher dispatcher = Minecraft.getInstance()
				.getBlockRenderer();
		return buildModel(dispatcher.getBlockModel(renderedState), renderedState);
	}

	private IModel buildModel(IBakedModel model, BlockState renderedState) {
		return buildModel(model, renderedState, new MatrixStack());
	}

	private IModel buildModel(IBakedModel model, BlockState referenceState, MatrixStack ms) {

		return new BlockModel(modelFormat, model, referenceState, ms);
	}
}
