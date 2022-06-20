package com.jozufozu.flywheel.core;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.backend.gl.GlNumericType;
import com.jozufozu.flywheel.backend.gl.buffer.GlBufferType;
import com.jozufozu.flywheel.backend.gl.buffer.MappedBuffer;
import com.jozufozu.flywheel.backend.gl.buffer.MappedGlBuffer;
import com.jozufozu.flywheel.backend.model.ElementBuffer;
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

	private final MappedGlBuffer ebo;
	private int quadCapacity;

	public QuadConverter() {
		this.ebo = new MappedGlBuffer(GlBufferType.ELEMENT_ARRAY_BUFFER);
		this.quadCapacity = 0;
	}

	public ElementBuffer quads2Tris(int quads) {
		int indexCount = quads * 6;

		if (quads > quadCapacity) {
			ebo.ensureCapacity((long) indexCount * GlNumericType.UINT.getByteWidth());

			try (MappedBuffer map = ebo.map()) {
				ByteBuffer indices = map.unwrap();

				fillBuffer(indices, quads);
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

	private void fillBuffer(ByteBuffer indices, int quads) {
		long addr = MemoryUtil.memAddress(indices);
		int numVertices = 4 * quads;
		int baseVertex = 0;
		while (baseVertex < numVertices) {
			// writeQuadIndices(indices, baseVertex);
			writeQuadIndicesUnsafe(addr, baseVertex);

			baseVertex += 4;
			addr += 6 * 4;
		}
		// ((Buffer) indices).flip();
	}

	private void writeQuadIndices(ByteBuffer indices, int baseVertex) {
		// triangle a
		indices.putInt(baseVertex);
		indices.putInt(baseVertex + 1);
		indices.putInt(baseVertex + 2);
		// triangle b
		indices.putInt(baseVertex);
		indices.putInt(baseVertex + 2);
		indices.putInt(baseVertex + 3);
	}

	private void writeQuadIndicesUnsafe(long addr, int baseVertex) {
		// triangle a
		MemoryUtil.memPutInt(addr, baseVertex);
		MemoryUtil.memPutInt(addr + 4, baseVertex + 1);
		MemoryUtil.memPutInt(addr + 8, baseVertex + 2);
		// triangle b
		MemoryUtil.memPutInt(addr + 12, baseVertex);
		MemoryUtil.memPutInt(addr + 16, baseVertex + 2);
		MemoryUtil.memPutInt(addr + 20, baseVertex + 3);
	}

	// make sure this gets reset first so it has a chance to repopulate
	public static void onRendererReload(ReloadRenderersEvent event) {
		if (INSTANCE != null) {
			INSTANCE.delete();
			INSTANCE = null;
		}
	}
}
