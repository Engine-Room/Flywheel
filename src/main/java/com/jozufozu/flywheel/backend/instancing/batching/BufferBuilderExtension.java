package com.jozufozu.flywheel.backend.instancing.batching;

import java.nio.ByteBuffer;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;

/**
 * Duck interface used on {@link BufferBuilder} to provide lower level access to the backing memory.
 *
 * @see com.jozufozu.flywheel.mixin.BufferBuilderMixin
 */
public interface BufferBuilderExtension {

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
}
