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
import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.instancer.Instancer;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.instancing.InstancerKey;
import com.jozufozu.flywheel.core.model.Mesh;
import com.jozufozu.flywheel.core.model.Model;

public class InstancingDrawManager {

	private final Map<InstancerKey<?>, GPUInstancer<?>> instancers = new HashMap<>();
	private final List<UninitializedModel> uninitializedModels = new ArrayList<>();
	private final List<GPUInstancer<?>> allInstancers = new ArrayList<>();
	private final Map<RenderStage, DrawSet> renderLists = new EnumMap<>(RenderStage.class);
	private final Map<VertexType, InstancedMeshPool> meshPools = new HashMap<>();

	public DrawSet get(RenderStage stage) {
		return renderLists.getOrDefault(stage, DrawSet.EMPTY);
	}

	@SuppressWarnings("unchecked")
	public <D extends InstancedPart> Instancer<D> getInstancer(StructType<D> type, Model model) {
		InstancerKey<D> key = new InstancerKey<>(type, model);
		GPUInstancer<D> instancer = (GPUInstancer<D>) instancers.get(key);
		if (instancer == null) {
			instancer = new GPUInstancer<>(type);
			instancers.put(key, instancer);
			uninitializedModels.add(new UninitializedModel(instancer, model));
		}
		return instancer;
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
		instancers.clear();

		meshPools.values()
				.forEach(InstancedMeshPool::delete);
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
			DrawCall drawCall = new DrawCall(instancer, entry.getKey(), alloc(entry.getValue()));
			var material = drawCall.getMaterial();
			var shaderState = new ShaderState(material, drawCall.getVertexType(), drawCall.instancer.type);

			renderLists.computeIfAbsent(material.getRenderStage(), DrawSet::new)
					.put(shaderState, drawCall);
		}
		allInstancers.add(instancer);
	}

	private InstancedMeshPool.BufferedMesh alloc(Mesh mesh) {
		return meshPools.computeIfAbsent(mesh.getVertexType(), InstancedMeshPool::new)
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

		public void put(ShaderState shaderState, DrawCall drawCall) {
			drawCalls.put(shaderState, drawCall);
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
