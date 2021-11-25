package com.jozufozu.flywheel.util;

import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Consumer;

import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.chunk.LevelChunk;

/**
 * Helper class for iterating over all loaded chunks.
 */
public class ChunkIter {

	private static final WeakHashMap<BlockGetter, AtomicReferenceArray<LevelChunk>> storages = new WeakHashMap<>();

	/**
	 * Iterate over all loaded chunks in a level.
	 */
	public static void forEachChunk(BlockGetter level, Consumer<LevelChunk> consumer) {
		AtomicReferenceArray<LevelChunk> storage = storages.get(level);

		if (storage == null)
			return;

		for (int i = 0; i < storage.length(); i++) {
			LevelChunk chunk = storage.get(i);
			if (chunk != null) {
				consumer.accept(chunk);
			}
		}
	}

	// INTERNAL MAINTENANCE METHODS BELOW

	public static void _putStorageReference(BlockGetter level, AtomicReferenceArray<LevelChunk> storage) {
		storages.put(level, storage);
	}

	public static void _unload(BlockGetter world) {
		storages.remove(world);
	}
}
