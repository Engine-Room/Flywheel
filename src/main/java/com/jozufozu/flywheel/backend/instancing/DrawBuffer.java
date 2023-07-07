package com.jozufozu.flywheel.backend.instancing;

import java.nio.ByteBuffer;

import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.backend.model.BufferBuilderExtension;
import com.jozufozu.flywheel.backend.model.DirectVertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderType;

/**
 * A byte buffer that can be used to draw vertices through a {@link DirectVertexConsumer}.
 *
 * The number of vertices needs to be known ahead of time.
 */
public class DrawBuffer {

	private final RenderType parent;
	private ByteBuffer backingBuffer;
	private int expectedVertices;

	@ApiStatus.Internal
	public DrawBuffer(RenderType parent) {
		this.parent = parent;
	}

	/**
	 * Creates a direct vertex consumer that can be used to write vertices into this buffer.
	 * @param vertexCount The number of vertices to reserve memory for.
	 * @return A direct vertex consumer.
	 * @throws IllegalStateException If the buffer is already in use.
	 */
	public DirectVertexConsumer begin(int vertexCount) {
		if (expectedVertices != 0) {
			throw new IllegalStateException("Already drawing");
		}

		this.expectedVertices = vertexCount;

		VertexFormat format = parent.format();

		// Add one extra vertex to uphold the vanilla assumption that BufferBuilders have at least
		// enough buffer space for one more vertex. Rubidium checks for this extra space when popNextBuffer
		// is called and reallocates the buffer if there is not space for one more vertex.
		int byteSize = format.getVertexSize() * (vertexCount + 1);

		if (backingBuffer == null) {
			backingBuffer = MemoryUtil.memAlloc(byteSize);
		}
		if (byteSize > backingBuffer.capacity()) {
			backingBuffer = MemoryUtil.memRealloc(backingBuffer, byteSize);
		}

		return new DirectVertexConsumer(backingBuffer, format, vertexCount);
	}

	/**
	 * Injects the backing buffer into the given builder and prepares it for rendering.
	 * @param bufferBuilder The buffer builder to inject into.
	 */
	public void inject(BufferBuilderExtension bufferBuilder) {
		bufferBuilder.flywheel$injectForRender(backingBuffer, parent.format(), expectedVertices);
	}

	/**
	 * @return {@code true} if the buffer has any vertices.
	 */
	public boolean hasVertices() {
		return expectedVertices > 0;
	}

	/**
	 * Reset the draw buffer to have no vertices.
	 *
	 * Does not clear the backing buffer.
	 */
	public void reset() {
		this.expectedVertices = 0;
	}
}
