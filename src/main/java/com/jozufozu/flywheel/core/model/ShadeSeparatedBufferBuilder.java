package com.jozufozu.flywheel.core.model;

import java.nio.ByteBuffer;

import com.jozufozu.flywheel.backend.model.BufferBuilderExtension;
import com.jozufozu.flywheel.fabric.helper.BufferBuilderHelper;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.datafixers.util.Pair;

public class ShadeSeparatedBufferBuilder extends BufferBuilder {
	protected int unshadedStartVertex;

	public ShadeSeparatedBufferBuilder(int capacity) {
		super(capacity);
	}

	public void appendUnshadedVertices(BufferBuilder unshadedBuilder) {
		Pair<DrawState, ByteBuffer> data = unshadedBuilder.popNextBuffer();
		ByteBuffer buffer = data.getSecond();
		BufferBuilderHelper.fixByteOrder(unshadedBuilder, buffer);
		unshadedStartVertex = ((BufferBuilderExtension) this).flywheel$getVertices();
		((BufferBuilderExtension) this).flywheel$appendBufferUnsafe(buffer);
	}

	public int getUnshadedStartVertex() {
		return unshadedStartVertex;
	}
}
