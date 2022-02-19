package com.jozufozu.flywheel.backend.model;

import java.nio.ByteBuffer;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;

/**
 * Duck interface used on {@link BufferBuilder} to provide lower level access to the backing memory.
 *
 * @see com.jozufozu.flywheel.mixin.BufferBuilderMixin
 */
public interface BufferBuilderExtension {

	int flywheel$getVertices();

	/**
	 * Frees the internal ByteBuffer, if it exists.
	 */
	void flywheel$freeBuffer();

	/**
	 * Prepares the BufferBuilder for drawing the contents of the given buffer.
	 * @param buffer The buffer to draw.
	 * @param format The format of the buffer.
	 * @param vertexCount The number of vertices in the buffer.
	 */
	void flywheel$injectForRender(ByteBuffer buffer, VertexFormat format, int vertexCount);

	/**
	 * Appends the remaining bytes from the given buffer to this BufferBuilder.
	 * @param buffer The buffer from which to copy bytes.
	 * @throws IllegalStateException If this BufferBuilder is not started or is the process of writing a vertex
	 * @throws IllegalArgumentException If the given buffer does not contain a whole number of vertices
	 */
	void flywheel$appendBufferUnsafe(ByteBuffer buffer);
}
