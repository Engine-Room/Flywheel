package com.jozufozu.flywheel.lib.model.baked;

import java.util.List;

import com.google.common.collect.ImmutableList;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.renderer.RenderType;

class ChunkLayerSortedListBuilder<T> {
	private static final ThreadLocal<ChunkLayerSortedListBuilder<?>> THREAD_LOCAL = ThreadLocal.withInitial(ChunkLayerSortedListBuilder::new);

	@SuppressWarnings("unchecked")
	private final ObjectArrayList<T>[] lists = new ObjectArrayList[BakedModelBufferer.CHUNK_LAYER_AMOUNT];

	private ChunkLayerSortedListBuilder() {
		for (int layerIndex = 0; layerIndex < BakedModelBufferer.CHUNK_LAYER_AMOUNT; layerIndex++) {
			ObjectArrayList<T> list = new ObjectArrayList<>();
			lists[layerIndex] = list;
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> ChunkLayerSortedListBuilder<T> getThreadLocal() {
		return (ChunkLayerSortedListBuilder<T>) THREAD_LOCAL.get();
	}

	public void add(RenderType renderType, T obj) {
		int layerIndex = renderType.getChunkLayerId();
		if (layerIndex == -1) {
			throw new IllegalArgumentException("RenderType '" + renderType + "' is not a chunk layer");
		}
		List<T> list = lists[layerIndex];
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
