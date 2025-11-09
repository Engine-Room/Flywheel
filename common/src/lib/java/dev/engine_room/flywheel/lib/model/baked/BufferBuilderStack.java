package dev.engine_room.flywheel.lib.model.baked;

import com.mojang.blaze3d.vertex.BufferBuilder;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;

class BufferBuilderStack {
	private static final int INITIAL_CAPACITY_VERTICES = 256;

	private int nextBufferBuilderIndex = 0;
	private final ReferenceArrayList<BufferBuilder> bufferBuilders = new ReferenceArrayList<>();

	BufferBuilder getOrCreateBufferBuilder() {
		BufferBuilder bufferBuilder;
		if (nextBufferBuilderIndex < bufferBuilders.size()) {
			bufferBuilder = bufferBuilders.get(nextBufferBuilderIndex);
		} else {
			// Need to allocate at least some memory up front, as BufferBuilder internally
			// only calls `ensureCapacity` after writing a vertex.
			bufferBuilder = new BufferBuilder(INITIAL_CAPACITY_VERTICES);
			bufferBuilders.add(bufferBuilder);
		}
		nextBufferBuilderIndex++;
		return bufferBuilder;
	}

	public void reset() {
		nextBufferBuilderIndex = 0;
	}
}
