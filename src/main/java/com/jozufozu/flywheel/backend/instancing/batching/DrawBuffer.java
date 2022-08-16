package com.jozufozu.flywheel.backend.instancing.batching;

import java.nio.ByteBuffer;

import org.jetbrains.annotations.ApiStatus;

import com.jozufozu.flywheel.api.vertex.ReusableVertexList;
import com.jozufozu.flywheel.api.vertex.VertexListProvider;
import com.jozufozu.flywheel.backend.memory.MemoryBlock;
import com.jozufozu.flywheel.core.vertex.VertexListProviderRegistry;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderType;

/**
 * A byte buffer that can be used to draw vertices through multiple {@link ReusableVertexList}s.
 *
 * The number of vertices needs to be known ahead of time.
 */
public class DrawBuffer {
	private final RenderType parent;
	private final VertexFormat format;
	private final int stride;
	private final VertexListProvider provider;

	private MemoryBlock memory;
	private ByteBuffer buffer;

	private int expectedVertices;

	@ApiStatus.Internal
	public DrawBuffer(RenderType parent) {
		this.parent = parent;
		format = parent.format();
		stride = format.getVertexSize();
		provider = VertexListProviderRegistry.getOrInfer(format);
	}

	/**
	 * Prepares this buffer by initializing a block of memory.
	 * @param vertexCount The number of vertices to reserve memory for.
	 * @throws IllegalStateException If the buffer is already in use.
	 */
	public void prepare(int vertexCount) {
		if (expectedVertices != 0) {
			throw new IllegalStateException("Already drawing!");
		}

		this.expectedVertices = vertexCount;

		// Add one extra vertex to uphold the vanilla assumption that BufferBuilders have at least
		// enough buffer space for one more vertex. Rubidium checks for this extra space when popNextBuffer
		// is called and reallocates the buffer if there is not space for one more vertex.
		int byteSize = stride * (vertexCount + 1);

		if (memory == null) {
			memory = MemoryBlock.malloc(byteSize);
			buffer = memory.asBuffer();
		} else if (byteSize > memory.size()) {
			memory = memory.realloc(byteSize);
			buffer = memory.asBuffer();
		}

		memory.clear();
	}

	public ReusableVertexList slice(int startVertex, int vertexCount) {
		ReusableVertexList vertexList = provider.createVertexList();
		vertexList.ptr(memory.ptr() + startVertex * stride);
		vertexList.setVertexCount(vertexCount);
		return vertexList;
	}

	/**
	 * Injects the backing buffer into the given builder and prepares it for rendering.
	 * @param bufferBuilder The buffer builder to inject into.
	 */
	public void inject(BufferBuilderExtension bufferBuilder) {
		buffer.clear();
		bufferBuilder.flywheel$injectForRender(buffer, format, expectedVertices);
	}

	public int getVertexCount() {
		return expectedVertices;
	}

	/**
	 * @return {@code true} if the buffer has any vertices.
	 */
	public boolean hasVertices() {
		return expectedVertices > 0;
	}

	/**
	 * Reset the draw buffer to have no vertices.<p>
	 *
	 * Does not clear the backing buffer.
	 */
	public void reset() {
		this.expectedVertices = 0;
	}

	public void free() {
		buffer = null;
		memory.free();
	}
}
