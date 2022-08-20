package com.jozufozu.flywheel.backend.instancing.batching;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.jozufozu.flywheel.api.RenderStage;
import com.jozufozu.flywheel.core.model.Model;

import net.minecraft.client.renderer.RenderType;

public class BatchingDrawManager {

	private final List<UninitializedModel> uninitializedModels = new ArrayList<>();
	private final List<CPUInstancer<?>> allInstancers = new ArrayList<>();
	public final Map<RenderStage, TransformSet> renderLists = new EnumMap<>(RenderStage.class);
	public final BatchDrawingTracker batchTracker = new BatchDrawingTracker();

	public TransformSet get(RenderStage stage) {
		return renderLists.getOrDefault(stage, TransformSet.EMPTY);
	}

	public void create(CPUInstancer<?> instancer, Model model) {
		uninitializedModels.add(new UninitializedModel(instancer, model));
	}

	public void flush() {
		for (var model : uninitializedModels) {
			add(model.instancer(), model.model());
		}
		uninitializedModels.clear();
	}

	public void delete() {
		allInstancers.forEach(CPUInstancer::delete);
		allInstancers.clear();
	}

	public void clearInstancers() {
		allInstancers.forEach(CPUInstancer::clear);
	}

	private void add(CPUInstancer<?> instancer, Model model) {
		var meshes = model.getMeshes();
		for (var entry : meshes.entrySet()) {
			TransformCall<?> transformCall = new TransformCall<>(instancer, entry.getKey(), entry.getValue());
			var material = transformCall.getMaterial();
			var renderType = material.getBatchingRenderType();

//			renderLists.computeIfAbsent(material.getRenderStage(), TransformSet::new)
			renderLists.computeIfAbsent(RenderStage.AFTER_FINAL_END_BATCH, TransformSet::new)
					.put(renderType, transformCall);
		}
		allInstancers.add(instancer);
	}

	public static class TransformSet implements Iterable<Map.Entry<RenderType, Collection<TransformCall<?>>>> {

		public static final TransformSet EMPTY = new TransformSet(ImmutableListMultimap.of());

		final ListMultimap<RenderType, TransformCall<?>> transformCalls;

		public TransformSet(RenderStage renderStage) {
			transformCalls = ArrayListMultimap.create();
		}

		public TransformSet(ListMultimap<RenderType, TransformCall<?>> transformCalls) {
			this.transformCalls = transformCalls;
		}

		public void put(RenderType shaderState, TransformCall<?> transformCall) {
			transformCalls.put(shaderState, transformCall);
		}

		public boolean isEmpty() {
			return transformCalls.isEmpty();
		}

		@NotNull
		@Override
		public Iterator<Map.Entry<RenderType, Collection<TransformCall<?>>>> iterator() {
			return transformCalls.asMap()
					.entrySet()
					.iterator();
		}
	}

	private record UninitializedModel(CPUInstancer<?> instancer, Model model) {
	}
}
