package com.jozufozu.flywheel.lib.model.baked;

import java.util.List;

import com.google.common.collect.ImmutableList;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.client.renderer.RenderType;

class ChunkLayerSortedListBuilder<T> {
	private static final ThreadLocal<ChunkLayerSortedListBuilder<?>> THREAD_LOCAL = ThreadLocal.withInitial(ChunkLayerSortedListBuilder::new);

	@SuppressWarnings("unchecked")
	private final ObjectArrayList<T>[] lists = new ObjectArrayList[BakedModelBufferer.CHUNK_LAYER_AMOUNT];
	private final Reference2ReferenceMap<RenderType, ObjectArrayList<T>> map = new Reference2ReferenceOpenHashMap<>();

	private ChunkLayerSortedListBuilder() {
		for (int layerIndex = 0; layerIndex < BakedModelBufferer.CHUNK_LAYER_AMOUNT; layerIndex++) {
			RenderType renderType = BakedModelBufferer.CHUNK_LAYERS[layerIndex];
			ObjectArrayList<T> list = new ObjectArrayList<>();
			lists[layerIndex] = list;
			map.put(renderType, list);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> ChunkLayerSortedListBuilder<T> getThreadLocal() {
		return (ChunkLayerSortedListBuilder<T>) THREAD_LOCAL.get();
	}

	public void add(RenderType renderType, T obj) {
		List<T> list = map.get(renderType);
		if (list == null) {
			throw new IllegalArgumentException("RenderType '" + renderType + "' is not a chunk layer");
		}
		list.add(obj);
	}

	@SuppressWarnings("unchecked")
	public ImmutableList<T> build() {
		int size = 0;
		for (ObjectArrayList<T> list : lists) {
			size += list.size();
		}

		T[] array = (T[]) new Object[size];
		int destPos = 0;
		for (ObjectArrayList<T> list : lists) {
			System.arraycopy(list.elements(), 0, array, destPos, list.size());
			destPos += list.size();
			list.clear();
		}

		return ImmutableList.copyOf(array);
	}
}
