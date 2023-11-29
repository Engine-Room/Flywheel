package com.jozufozu.flywheel.backend.engine.batching;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.jozufozu.flywheel.api.event.ReloadRenderersEvent;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.vertex.ReusableVertexList;
import com.jozufozu.flywheel.api.vertex.VertexListProvider;
import com.jozufozu.flywheel.extension.BufferBuilderExtension;
import com.jozufozu.flywheel.extension.RenderTypeExtension;
import com.jozufozu.flywheel.lib.memory.MemoryBlock;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;

/**
 * A byte buffer that can be used to draw vertices through multiple {@link ReusableVertexList}s.
 * <br>
 * Note: The number of vertices needs to be known ahead of time.
 */
public class DrawBuffer {
	private static final List<DrawBuffer> ALL = new ArrayList<>();

	private final RenderType renderType;
	private final VertexFormat format;
	private final int stride;
	private final boolean sortOnUpload;
	private final VertexListProvider provider;

	private MemoryBlock data;
	private ByteBuffer buffer;

	private boolean prepared;
	private int vertexCount;
	private int verticesToDraw;

	public DrawBuffer(RenderType renderType, VertexFormat format, int stride, boolean sortOnUpload, VertexListProvider provider) {
		this.renderType = renderType;
		this.format = format;
		this.stride = stride;
		this.sortOnUpload = sortOnUpload;
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
		verticesToDraw = this.vertexCount;

		// Add one extra vertex to uphold the vanilla assumption that BufferBuilders have at least
		// enough buffer space for one more vertex. Rubidium checks for this extra space when popNextBuffer
		// is called and reallocates the buffer if there is not space for one more vertex.
		int byteSize = stride * (vertexCount + 1);

		// We'll need to allocate space for the index buffer if this render type needs sorting.
		if (sortOnUpload) {
			int i = renderType.mode()
					.indexCount(vertexCount);
			VertexFormat.IndexType indexType = VertexFormat.IndexType.least(i);
			int extraBytes = Mth.roundToward(i * indexType.bytes, 4);
			byteSize += extraBytes;
		}

		if (data == null) {
			data = MemoryBlock.malloc(byteSize);
			buffer = data.asBuffer();
		} else if (byteSize > data.size()) {
			data = data.realloc(byteSize);
			buffer = data.asBuffer();
		}

		prepared = true;
	}

	public ReusableVertexList slice(int startVertex, int vertexCount) {
		if (!prepared) {
			throw new IllegalStateException("Cannot slice DrawBuffer that is not prepared!");
		}

		if (startVertex + vertexCount > this.vertexCount) {
			throw new IndexOutOfBoundsException("Vertex count greater than allocated: " + startVertex + " + " + vertexCount + " > " + this.vertexCount);
		}

		ReusableVertexList vertexList = provider.createVertexList();
		vertexList.ptr(ptrForVertex(startVertex));
		vertexList.vertexCount(vertexCount);
		return vertexList;
	}

	public long ptrForVertex(long startVertex) {
		return data.ptr() + startVertex * stride;
	}

	public void verticesToDraw(int verticesToDraw) {
		this.verticesToDraw = verticesToDraw;
	}

	/**
	 * Injects the backing buffer into the given builder and prepares it for rendering.
	 *
	 * @param bufferBuilder The buffer builder to inject into.
	 */
	public void inject(BufferBuilderExtension bufferBuilder) {
		if (!prepared) {
			throw new IllegalStateException("Cannot inject DrawBuffer that is not prepared!");
		}

		buffer.clear();
		bufferBuilder.flywheel$injectForRender(buffer, format, verticesToDraw);
	}

	public RenderType getRenderType() {
		return renderType;
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

	public int getVerticesToDraw() {
		return verticesToDraw;
	}

	/**
	 * @return {@code true} if the buffer has any vertices to draw.
	 */
	public boolean hasVertices() {
		return verticesToDraw > 0;
	}

	/**
	 * Reset the draw buffer to have no vertices.<p>
	 *
	 * Does not clear the backing buffer.
	 */
	public void reset() {
		prepared = false;
		vertexCount = 0;
		verticesToDraw = 0;
	}

	public void free() {
		reset();

		if (data == null) {
			return;
		}

		data.free();
		data = null;
		buffer = null;
	}

	public static void onReloadRenderers(ReloadRenderersEvent event) {
		ALL.forEach(DrawBuffer::free);
	}

	public static DrawBuffer get(RenderType renderType, RenderStage stage) {
		return RenderTypeExtension.getDrawBufferSet(renderType)
				.getBuffer(stage);
	}
}
