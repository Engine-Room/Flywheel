package com.jozufozu.flywheel.core.model;

import java.nio.ByteBuffer;

import com.jozufozu.flywheel.backend.model.BufferBuilderExtension;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.datafixers.util.Pair;

public class ShadeSeparatedBufferBuilder extends BufferBuilder {
	protected int unshadedStartVertex;

	public ShadeSeparatedBufferBuilder(int capacity) {
		super(capacity);
	}

	public void appendUnshadedVertices(BufferBuilder unshadedBuilder) {
		RenderedBuffer renderedBuffer = unshadedBuilder.endOrDiscardIfEmpty();

		if (renderedBuffer == null) {
			return;
		}

		// FIXME: Unshaded indices
		ByteBuffer buffer = renderedBuffer.vertexBuffer();
		unshadedStartVertex = ((BufferBuilderExtension) this).flywheel$getVertices();
		((BufferBuilderExtension) this).flywheel$appendBufferUnsafe(buffer);
	}

	public int getUnshadedStartVertex() {
		return unshadedStartVertex;
	}
}
