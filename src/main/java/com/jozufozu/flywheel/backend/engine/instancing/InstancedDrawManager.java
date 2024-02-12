package com.jozufozu.flywheel.backend.engine.instancing;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.model.Mesh;
import com.jozufozu.flywheel.backend.engine.InstancerKey;
import com.jozufozu.flywheel.backend.engine.InstancerStorage;

public class InstancedDrawManager extends InstancerStorage<InstancedInstancer<?>> {
	/**
	 * The set of draw calls to make in each {@link RenderStage}.
	 */
	private final Map<RenderStage, DrawSet> drawSets = new EnumMap<>(RenderStage.class);
	/**
	 * A map of vertex types to their mesh pools.
	 */
	private final InstancedMeshPool meshPool = new InstancedMeshPool();
	private final EboCache eboCache = new EboCache();

	public DrawSet get(RenderStage stage) {
		return drawSets.getOrDefault(stage, DrawSet.EMPTY);
	}

	public void flush() {
		super.flush();

		meshPool.flush();
	}

	public void delete() {
		instancers.values()
				.forEach(InstancedInstancer::delete);

		super.delete();

		meshPool.delete();

		drawSets.values()
				.forEach(DrawSet::delete);
		drawSets.clear();

		eboCache.invalidate();
	}

	private InstancedMeshPool.BufferedMesh alloc(Mesh mesh) {
		return meshPool.alloc(mesh, eboCache);
	}

	@Override
	protected <I extends Instance> InstancedInstancer<I> create(InstancerKey<I> key) {
		return new InstancedInstancer<>(key.type(), key.context());
	}

	@Override
	protected <I extends Instance> void initialize(InstancerKey<I> key, InstancedInstancer<?> instancer) {
		instancer.init();

		DrawSet drawSet = drawSets.computeIfAbsent(key.stage(), DrawSet::new);

		var meshes = key.model()
				.meshes();
		for (var entry : meshes.entrySet()) {
			var mesh = alloc(entry.getValue());

			ShaderState shaderState = new ShaderState(entry.getKey(), key.type(), key.context());
			DrawCall drawCall = new DrawCall(instancer, mesh, shaderState);

			drawSet.put(shaderState, drawCall);
			instancer.addDrawCall(drawCall);
		}
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

		@Override
		public Iterator<Map.Entry<ShaderState, Collection<DrawCall>>> iterator() {
			return drawCalls.asMap()
					.entrySet()
					.iterator();
		}
	}
}
