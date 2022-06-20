package com.jozufozu.flywheel.backend.instancing.instancing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
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

	protected final List<InstancedModel<D>> uninitialized = new ArrayList<>();

	private final ListMultimap<RenderType, Renderable> renderLists = ArrayListMultimap.create();

	public GPUInstancerFactory(StructType<D> type) {
		this.type = type;
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
		renderLists.clear();
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

	public void init() {
		for (var instanced : uninitialized) {
			instanced.init();

			for (Renderable renderable : instanced.getLayers()) {
				renderLists.put(renderable.getMaterial()
						.getRenderType(), renderable);
			}
		}
		uninitialized.clear();
	}

	private InstancedModel<D> createInstancer(ModelSupplier model) {
		var instancer = new InstancedModel<>(type, model);
		uninitialized.add(instancer);
		return instancer;
	}

	/**
	 * Adds all the RenderTypes that this InstancerFactory will render to the given set.
	 * @param layersToProcess The set of RenderTypes that the InstancingEngine will process.
	 */
	public void gatherLayers(Set<RenderType> layersToProcess) {
		layersToProcess.addAll(renderLists.keySet());
	}

	public List<Renderable> getRenderList(RenderType type) {
		var out = renderLists.get(type);
		out.removeIf(Renderable::shouldRemove);
		return out;
	}
}
