package com.jozufozu.flywheel.lib.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.event.ReloadRenderersEvent;
import com.jozufozu.flywheel.gl.GlNumericType;
import com.jozufozu.flywheel.gl.buffer.ElementBuffer;
import com.jozufozu.flywheel.gl.buffer.GlBuffer;
import com.jozufozu.flywheel.gl.buffer.GlBufferUsage;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.VertexFormat;

import it.unimi.dsi.fastutil.ints.Int2ReferenceArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;

/**
 * A class to manage EBOs that index quads as triangles.
 */
public class QuadConverter {
	private static QuadConverter INSTANCE;

	@NotNull
	public static QuadConverter getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new QuadConverter();
		}

		return INSTANCE;
	}

	@Nullable
	public static QuadConverter getNullable() {
		return INSTANCE;
	}

	private final Int2ReferenceMap<ElementBuffer> cache = new Int2ReferenceArrayMap<>();
	private final int ebo;
	private int quadCapacity;

	public QuadConverter() {
		this.ebo = GlBuffer.IMPL.create();
		this.quadCapacity = 0;
	}

	public ElementBuffer quads2Tris(int quads) {
		if (quads > quadCapacity) {
			grow(quads * 2);
		}

		return cache.computeIfAbsent(quads, this::createElementBuffer);
	}

	@NotNull
	private ElementBuffer createElementBuffer(int quads) {
		return new ElementBuffer(ebo, quads * 6, VertexFormat.IndexType.INT);
	}

	private void grow(int quads) {
		int byteSize = quads * 6 * GlNumericType.UINT.getByteWidth();
		final long ptr = MemoryUtil.nmemAlloc(byteSize);

		fillBuffer(ptr, quads);

		GlBuffer.IMPL.data(ebo, byteSize, ptr, GlBufferUsage.STATIC_DRAW.glEnum);

		MemoryUtil.nmemFree(ptr);

		this.quadCapacity = quads;
	}

	public void delete() {
		GlStateManager._glDeleteBuffers(ebo);
		this.cache.clear();
		this.quadCapacity = 0;
	}

	private void fillBuffer(long ptr, int quads) {
		int numVertices = 4 * quads;
		int baseVertex = 0;
		while (baseVertex < numVertices) {
			writeQuadIndicesUnsafe(ptr, baseVertex);

			baseVertex += 4;
			ptr += 6 * 4;
		}
	}

	private void writeQuadIndicesUnsafe(long ptr, int baseVertex) {
		// triangle a
		MemoryUtil.memPutInt(ptr, baseVertex);
		MemoryUtil.memPutInt(ptr + 4, baseVertex + 1);
		MemoryUtil.memPutInt(ptr + 8, baseVertex + 2);
		// triangle b
		MemoryUtil.memPutInt(ptr + 12, baseVertex);
		MemoryUtil.memPutInt(ptr + 16, baseVertex + 2);
		MemoryUtil.memPutInt(ptr + 20, baseVertex + 3);
	}

	// make sure this gets reset first, so it has a chance to repopulate
	public static void onReloadRenderers(ReloadRenderersEvent event) {
		if (INSTANCE != null) {
			INSTANCE.delete();
			INSTANCE = null;
		}
	}
}
