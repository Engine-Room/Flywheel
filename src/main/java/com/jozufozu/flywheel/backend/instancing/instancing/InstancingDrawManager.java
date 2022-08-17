package com.jozufozu.flywheel.backend.instancing.instancing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.jozufozu.flywheel.api.RenderStage;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.core.model.Mesh;
import com.jozufozu.flywheel.core.model.Model;

public class InstancingDrawManager {

	private final List<UninitializedModel> uninitializedModels = new ArrayList<>();
	private final List<GPUInstancer<?>> allInstancers = new ArrayList<>();
	private final Map<RenderStage, DrawSet> renderLists = new EnumMap<>(RenderStage.class);
	private final Map<VertexType, MeshPool> meshPools = new HashMap<>();

	public DrawSet get(RenderStage stage) {
		return renderLists.getOrDefault(stage, DrawSet.EMPTY);
	}

	public void create(GPUInstancer<?> gpuInstancer, Model model) {
		uninitializedModels.add(new UninitializedModel(gpuInstancer, model));
	}

	public void flush() {
		for (var model : uninitializedModels) {
			model.instancer()
					.init();

			add(model.instancer(), model.model());
		}
		uninitializedModels.clear();

		for (var pool : meshPools.values()) {
			pool.flush();
		}
	}

	public void delete() {
		meshPools.values()
				.forEach(MeshPool::delete);
		meshPools.clear();

		renderLists.values()
				.forEach(DrawSet::delete);
		renderLists.clear();

		allInstancers.forEach(GPUInstancer::delete);
		allInstancers.clear();
	}

	public void clearInstancers() {
		allInstancers.forEach(GPUInstancer::clear);
	}

	private void add(GPUInstancer<?> instancer, Model model) {
		var meshes = model.getMeshes();
		for (var entry : meshes.entrySet()) {
			DrawCall layer = new DrawCall(instancer, entry.getKey(), alloc(entry.getValue()));
			var material = layer.getMaterial();
			var shaderState = new ShaderState(material, layer.getVertexType(), layer.instancer.type);

			renderLists.computeIfAbsent(material.getRenderStage(), DrawSet::new)
					.put(shaderState, layer);
		}
		allInstancers.add(instancer);
	}

	private MeshPool.BufferedMesh alloc(Mesh mesh) {
		return meshPools.computeIfAbsent(mesh.getVertexType(), MeshPool::new)
				.alloc(mesh);
	}

	public static class DrawSet implements Iterable<Map.Entry<ShaderState, Collection<DrawCall>>> {

		public static final DrawSet EMPTY = new DrawSet(ImmutableListMultimap.of());

		final ListMultimap<ShaderState, DrawCall> drawCalls;

		public DrawSet(RenderStage renderStage) {
			drawCalls = ArrayListMultimap.create();
		}

		public DrawSet(ListMultimap<ShaderState, DrawCall> drawCalls) {
			this.drawCalls = drawCalls;
		}

		private void delete() {
			drawCalls.values()
					.forEach(DrawCall::delete);
			drawCalls.clear();
		}

		public void put(ShaderState shaderState, DrawCall layer) {
			drawCalls.put(shaderState, layer);
		}

		public boolean isEmpty() {
			return drawCalls.isEmpty();
		}

		@NotNull
		@Override
		public Iterator<Map.Entry<ShaderState, Collection<DrawCall>>> iterator() {
			return drawCalls.asMap()
					.entrySet()
					.iterator();
		}
	}

	private record UninitializedModel(GPUInstancer<?> instancer, Model model) {

	}
}
