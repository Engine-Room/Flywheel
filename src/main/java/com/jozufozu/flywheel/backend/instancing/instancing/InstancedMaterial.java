package com.jozufozu.flywheel.backend.instancing.instancing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.jozufozu.flywheel.api.InstanceData;
import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.Material;
import com.jozufozu.flywheel.api.struct.Instanced;
import com.jozufozu.flywheel.backend.model.MeshAllocator;
import com.jozufozu.flywheel.core.model.ModelSupplier;

import net.minecraft.client.renderer.RenderType;

/**
 * A collection of Instancers that all have the same format.
 * @param <D>
 */
public class InstancedMaterial<D extends InstanceData> implements Material<D> {

	protected final Map<ModelSupplier, InstancedModel<D>> models = new HashMap<>();
	protected final Instanced<D> type;

	protected final List<InstancedModel<D>> uninitialized = new ArrayList<>();

	protected final Multimap<RenderType, Renderable> renderables = ArrayListMultimap.create();

	public InstancedMaterial(Instanced<D> type) {
		this.type = type;
	}

	@Override
	public Instancer<D> model(ModelSupplier modelKey) {
		return models.computeIfAbsent(modelKey, this::createInstancer).instancer;
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

	public boolean nothingToRender() {
		return models.size() > 0 && models.values()
				.stream()
				.allMatch(InstancedModel::isEmpty);
	}

	public void delete() {
		models.values().forEach(InstancedModel::delete);
		models.clear();
		renderables.clear();
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

	public void init(MeshAllocator allocator) {
		for (var instanced : uninitialized) {

			var map = instanced.init(allocator);

			map.forEach((type, renderable) -> renderables.get(type).add(renderable));
		}
		uninitialized.clear();
	}

	public void renderIn(RenderType layer) {
		renderables.get(layer).forEach(Renderable::render);
	}

	public boolean anythingToRender(RenderType type) {
		return renderables.get(type).size() > 0;
	}

	private InstancedModel<D> createInstancer(ModelSupplier model) {
		var instancer = new InstancedModel<>(new GPUInstancer<>(type), model);
		uninitialized.add(instancer);
		return instancer;
	}
}
