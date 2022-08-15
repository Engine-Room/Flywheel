package com.jozufozu.flywheel.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.backend.gl.GlNumericType;
import com.jozufozu.flywheel.backend.gl.buffer.GlBuffer;
import com.jozufozu.flywheel.backend.gl.buffer.GlBufferType;
import com.jozufozu.flywheel.backend.gl.buffer.MappedBuffer;
import com.jozufozu.flywheel.backend.instancing.instancing.ElementBuffer;
import com.jozufozu.flywheel.event.ReloadRenderersEvent;

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

	private final GlBuffer ebo;
	private int quadCapacity;

	public QuadConverter() {
		this.ebo = new GlBuffer(GlBufferType.ELEMENT_ARRAY_BUFFER);
		this.quadCapacity = 0;
	}

	public ElementBuffer quads2Tris(int quads) {
		int indexCount = quads * 6;

		if (quads > quadCapacity) {
			ebo.ensureCapacity((long) indexCount * GlNumericType.UINT.getByteWidth());

			try (MappedBuffer map = ebo.map()) {
				fillBuffer(map.getPtr(), quads);
			}
			ebo.unbind();

			this.quadCapacity = quads;
		}

		return new ElementBuffer(ebo, indexCount, GlNumericType.UINT);
	}

	public void delete() {
		ebo.delete();
		this.quadCapacity = 0;
	}

	private void fillBuffer(long addr, int quads) {
		int numVertices = 4 * quads;
		int baseVertex = 0;
		while (baseVertex < numVertices) {
			writeQuadIndices(addr, baseVertex);

			baseVertex += 4;
			addr += 6 * 4;
		}
	}

	private void writeQuadIndices(long addr, int baseVertex) {
		// triangle a
		MemoryUtil.memPutInt(addr, baseVertex);
		MemoryUtil.memPutInt(addr + 4, baseVertex + 1);
		MemoryUtil.memPutInt(addr + 8, baseVertex + 2);
		// triangle b
		MemoryUtil.memPutInt(addr + 12, baseVertex);
		MemoryUtil.memPutInt(addr + 16, baseVertex + 2);
		MemoryUtil.memPutInt(addr + 20, baseVertex + 3);
	}

	// make sure this gets reset first, so it has a chance to repopulate
	public static void onRendererReload(ReloadRenderersEvent event) {
		if (INSTANCE != null) {
			INSTANCE.delete();
			INSTANCE = null;
		}
	}
}
