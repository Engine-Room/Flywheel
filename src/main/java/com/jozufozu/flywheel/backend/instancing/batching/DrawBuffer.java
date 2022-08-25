package com.jozufozu.flywheel.backend.instancing.batching;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.jozufozu.flywheel.api.vertex.ReusableVertexList;
import com.jozufozu.flywheel.api.vertex.VertexListProvider;
import com.jozufozu.flywheel.backend.memory.MemoryBlock;
import com.jozufozu.flywheel.event.ReloadRenderersEvent;
import com.jozufozu.flywheel.extension.BufferBuilderExtension;
import com.mojang.blaze3d.vertex.VertexFormat;

/**
 * A byte buffer that can be used to draw vertices through multiple {@link ReusableVertexList}s.
 *
 * The number of vertices needs to be known ahead of time.
 */
public class DrawBuffer {
	private static final List<DrawBuffer> ALL = new ArrayList<>();

	private final VertexFormat format;
	private final int stride;
	private final VertexListProvider provider;

	private MemoryBlock memory;
	private ByteBuffer buffer;

	private boolean prepared;
	private int vertexCount;

	DrawBuffer(VertexFormat format, int stride, VertexListProvider provider) {
		this.format = format;
		this.stride = stride;
		this.provider = provider;

		ALL.add(this);
	}

	/**
	 * Prepares this buffer by initializing a block of memory.
	 * @param vertexCount The number of vertices to reserve memory for.
	 * @throws IllegalStateException If the buffer is already in use.
	 */
	public void prepare(int vertexCount) {
		if (prepared) {
			throw new IllegalStateException("Cannot prepare DrawBuffer twice!");
		}

		this.vertexCount = vertexCount;

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
		prepared = true;
	}

	public ReusableVertexList slice(int startVertex, int vertexCount) {
		if (!prepared) {
			throw new IllegalStateException("Cannot slice DrawBuffer that is not prepared!");
		}

		ReusableVertexList vertexList = provider.createVertexList();
		vertexList.ptr(memory.ptr() + startVertex * vertexList.vertexStride());
		vertexList.vertexCount(vertexCount);
		return vertexList;
	}

	/**
	 * Injects the backing buffer into the given builder and prepares it for rendering.
	 * @param bufferBuilder The buffer builder to inject into.
	 */
	public void inject(BufferBuilderExtension bufferBuilder) {
		if (!prepared) {
			throw new IllegalStateException("Cannot inject DrawBuffer that is not prepared!");
		}

		buffer.clear();
		bufferBuilder.flywheel$injectForRender(buffer, format, vertexCount);
	}

	public VertexFormat getVertexFormat() {
		return format;
	}

	public boolean isPrepared() {
		return prepared;
	}

	public int getVertexCount() {
		return vertexCount;
	}

	/**
	 * @return {@code true} if the buffer has any vertices.
	 */
	public boolean hasVertices() {
		return vertexCount > 0;
	}

	/**
	 * Reset the draw buffer to have no vertices.<p>
	 *
	 * Does not clear the backing buffer.
	 */
	public void reset() {
		prepared = false;
		vertexCount = 0;
	}

	public void free() {
		if (memory == null) {
			return;
		}
		memory.free();
		memory = null;
		buffer = null;
	}

	public static void onReloadRenderers(ReloadRenderersEvent event) {
		ALL.forEach(DrawBuffer::free);
	}
}
