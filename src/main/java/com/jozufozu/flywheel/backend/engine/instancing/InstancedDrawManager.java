package com.jozufozu.flywheel.backend.engine.instancing;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.model.Mesh;
import com.jozufozu.flywheel.api.model.Model;
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
	protected <I extends Instance> InstancedInstancer<I> create(InstanceType<I> type) {
		return new InstancedInstancer<>(type);
	}

	@Override
	protected <I extends Instance> void add(InstancerKey<I> key, InstancedInstancer<?> instancer, Model model, RenderStage stage) {
		if (model.meshes()
				.isEmpty()) {
			// Don't bother allocating resources for models with no meshes.
			return;
		}

		instancer.init();

		DrawSet drawSet = drawSets.computeIfAbsent(stage, DrawSet::new);

		var meshes = model.meshes();
		for (var entry : meshes.entrySet()) {
			var mesh = alloc(entry.getValue());

			ShaderState shaderState = new ShaderState(entry.getKey(), instancer.type);
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

		@NotNull
		@Override
		public Iterator<Map.Entry<ShaderState, Collection<DrawCall>>> iterator() {
			return drawCalls.asMap()
					.entrySet()
					.iterator();
		}
	}
}
