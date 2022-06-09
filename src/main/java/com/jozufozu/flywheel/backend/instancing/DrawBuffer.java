package com.jozufozu.flywheel.backend.instancing;

import java.lang.ref.Cleaner;
import java.nio.ByteBuffer;

import com.jozufozu.flywheel.backend.FlywheelMemory;
import com.jozufozu.flywheel.backend.model.BufferBuilderExtension;
import com.jozufozu.flywheel.backend.model.DirectVertexConsumer;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderType;

/**
 * A byte buffer that can be used to draw vertices through a {@link DirectVertexConsumer}.
 *
 * The number of vertices needs to be known ahead of time.
 */
public class DrawBuffer implements AutoCloseable {

	private final RenderType parent;
	private ByteBuffer backingBuffer;
	private int expectedVertices;
	private Cleaner.Cleanable cleanable;

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

		int byteSize = format.getVertexSize() * vertexCount;

		if (backingBuffer == null) {
			backingBuffer = MemoryTracker.create(byteSize);
			cleanable = FlywheelMemory.track(this, backingBuffer);
		}
		if (byteSize > backingBuffer.capacity()) {
			cleanable.clean();
			backingBuffer = MemoryTracker.resize(backingBuffer, byteSize);
			cleanable = FlywheelMemory.track(this, backingBuffer);
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

	@Override
	public void close() {
		if (cleanable != null) {
			cleanable.clean();
		}
	}
}
