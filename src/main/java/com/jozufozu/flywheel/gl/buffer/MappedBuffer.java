package com.jozufozu.flywheel.gl.buffer;

import static org.lwjgl.opengl.GL30.GL_MAP_WRITE_BIT;
import static org.lwjgl.opengl.GL30.nglMapBufferRange;
import static org.lwjgl.system.MemoryUtil.NULL;

import org.lwjgl.opengl.GL15;
import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.gl.error.GlError;
import com.jozufozu.flywheel.gl.error.GlException;

public class MappedBuffer implements AutoCloseable {
	private final int glBuffer;
	private long ptr;

	public MappedBuffer(int glBuffer, long size) {
		this.glBuffer = glBuffer;

		GlBufferType.COPY_READ_BUFFER.bind(glBuffer);
		ptr = nglMapBufferRange(GlBufferType.COPY_READ_BUFFER.glEnum, 0, size, GL_MAP_WRITE_BIT);

		if (ptr == MemoryUtil.NULL) {
			throw new GlException(GlError.poll(), "Could not map buffer");
		}
	}

	public long ptr() {
		return ptr;
	}

	@Override
	public void close() {
		if (ptr == NULL) {
			return;
		}

		GlBufferType.COPY_READ_BUFFER.bind(glBuffer);
		GL15.glUnmapBuffer(GlBufferType.COPY_READ_BUFFER.glEnum);
		ptr = NULL;
	}
}
