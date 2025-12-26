package dev.engine_room.flywheel.lib.model.baked;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;

class ByteBufferBuilderStack {
	private static final int INITIAL_CAPACITY_BYTES = 256 * 32;

	private int nextBufferBuilderIndex = 0;
	private final ReferenceArrayList<ByteBufferBuilder> bufferBuilders = new ReferenceArrayList<>();

	ByteBufferBuilder nextOrCreate() {
		ByteBufferBuilder bufferBuilder;
		if (nextBufferBuilderIndex < bufferBuilders.size()) {
			bufferBuilder = bufferBuilders.get(nextBufferBuilderIndex);
		} else {
			// Need to allocate at least some memory up front, as BufferBuilder internally
			// only calls `ensureCapacity` after writing a vertex.
			bufferBuilder = new ByteBufferBuilder(INITIAL_CAPACITY_BYTES);
			bufferBuilders.add(bufferBuilder);
		}
		nextBufferBuilderIndex++;
		return bufferBuilder;
	}

	public void reset() {
		nextBufferBuilderIndex = 0;
	}
}
