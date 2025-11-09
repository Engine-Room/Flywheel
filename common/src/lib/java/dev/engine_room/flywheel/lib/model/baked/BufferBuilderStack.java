package dev.engine_room.flywheel.lib.model.baked;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.client.renderer.RenderType;

class BufferBuilderStack {
	private static final int INITIAL_CAPACITY_BYTES = 256 * 32;

	private int nextBufferBuilderIndex = 0;
	private final ReferenceArrayList<BufferBuilder> bufferBuilders = new ReferenceArrayList<>();

	private final RenderType renderType;

	BufferBuilderStack(RenderType renderType) {
		this.renderType = renderType;
	}

	BufferBuilder getOrCreateBufferBuilder() {
		BufferBuilder bufferBuilder;
		if (nextBufferBuilderIndex < bufferBuilders.size()) {
			bufferBuilder = bufferBuilders.get(nextBufferBuilderIndex);
		} else {
			// Need to allocate at least some memory up front, as BufferBuilder internally
			// only calls `ensureCapacity` after writing a vertex.
			bufferBuilder = new BufferBuilder(new ByteBufferBuilder(INITIAL_CAPACITY_BYTES), renderType.mode(), renderType.format());
			bufferBuilders.add(bufferBuilder);
		}
		nextBufferBuilderIndex++;
		return bufferBuilder;
	}

	public void reset() {
		nextBufferBuilderIndex = 0;
	}
}
