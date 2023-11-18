package com.jozufozu.flywheel.backend.engine.instancing;

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
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.instance.Instancer;
import com.jozufozu.flywheel.api.model.Mesh;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.engine.InstancerKey;

public class InstancedDrawManager {
	private final Map<InstancerKey<?>, InstancedInstancer<?>> instancers = new HashMap<>();
	private final List<UninitializedInstancer> uninitializedInstancers = new ArrayList<>();
	private final List<InstancedInstancer<?>> initializedInstancers = new ArrayList<>();
	private final Map<RenderStage, DrawSet> drawSets = new EnumMap<>(RenderStage.class);
	private final Map<VertexType, InstancedMeshPool> meshPools = new HashMap<>();
	private final EBOCache eboCache = new EBOCache();

	public DrawSet get(RenderStage stage) {
		return drawSets.getOrDefault(stage, DrawSet.EMPTY);
	}

	@SuppressWarnings("unchecked")
	public <I extends Instance> Instancer<I> getInstancer(InstanceType<I> type, Model model, RenderStage stage) {
		InstancerKey<I> key = new InstancerKey<>(type, model, stage);
		InstancedInstancer<I> instancer = (InstancedInstancer<I>) instancers.get(key);
		if (instancer == null) {
			instancer = new InstancedInstancer<>(type);
			instancers.put(key, instancer);
			uninitializedInstancers.add(new UninitializedInstancer(instancer, model, stage));
		}
		return instancer;
	}

	public void flush() {
		for (var instancer : uninitializedInstancers) {
			add(instancer.instancer(), instancer.model(), instancer.stage());
		}
		uninitializedInstancers.clear();

		for (var pool : meshPools.values()) {
			pool.flush();
		}
	}

	public void invalidate() {
		instancers.clear();

		meshPools.values()
				.forEach(InstancedMeshPool::delete);
		meshPools.clear();

		drawSets.values()
				.forEach(DrawSet::delete);
		drawSets.clear();

		initializedInstancers.forEach(InstancedInstancer::delete);
		initializedInstancers.clear();

		eboCache.invalidate();
	}

	public void clearInstancers() {
		initializedInstancers.forEach(InstancedInstancer::clear);
	}

	private void add(InstancedInstancer<?> instancer, Model model, RenderStage stage) {
		instancer.init();
		DrawSet drawSet = drawSets.computeIfAbsent(stage, DrawSet::new);
		var meshes = model.getMeshes();
		for (var entry : meshes.entrySet()) {
			var mesh = alloc(entry.getValue());
			ShaderState shaderState = new ShaderState(entry.getKey(), mesh.getVertexType(), instancer.type);
			DrawCall drawCall = new DrawCall(instancer, mesh);
			drawSet.put(shaderState, drawCall);
		}
		initializedInstancers.add(instancer);
	}

	private InstancedMeshPool.BufferedMesh alloc(Mesh mesh) {
		return meshPools.computeIfAbsent(mesh.vertexType(), InstancedMeshPool::new)
				.alloc(mesh, eboCache);
	}

	public static class DrawSet implements Iterable<Map.Entry<ShaderState, Collection<DrawCall>>> {
		public static final DrawSet EMPTY = new DrawSet(ImmutableListMultimap.of());

		private final ListMultimap<ShaderState, DrawCall> drawCalls;

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

	private record UninitializedInstancer(InstancedInstancer<?> instancer, Model model, RenderStage stage) {
	}
}
