package com.jozufozu.flywheel.backend.gl.buffer;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import com.jozufozu.flywheel.backend.gl.error.GlError;
import com.jozufozu.flywheel.backend.gl.error.GlException;

public class MappedGlBuffer extends GlBuffer {

	protected final GlBufferUsage usage;

	public MappedGlBuffer(GlBufferType type) {
		this(type, GlBufferUsage.STATIC_DRAW);
	}

	public MappedGlBuffer(GlBufferType type, GlBufferUsage usage) {
		super(type);
		this.usage = usage;
	}

	@Override
	public boolean ensureCapacity(long size) {
		if (size < 0) {
			throw new IllegalArgumentException("Size " + size + " < 0");
		}

		if (size == 0) {
			return false;
		}

		if (this.size == 0) {
			this.size = size;
			bind();
			GL32.glBufferData(type.glEnum, size, usage.glEnum);

			return true;
		}

		if (size > this.size) {
			var oldSize = this.size;
			this.size = size + growthMargin;

			realloc(oldSize, this.size);

			return true;
		}

		return false;
	}

	private void realloc(long oldSize, long newSize) {
		var oldHandle = handle();
		var newHandle = GL32.glGenBuffers();

		GlBufferType.COPY_READ_BUFFER.bind(oldHandle);
		type.bind(newHandle);

		GL32.glBufferData(type.glEnum, newSize, usage.glEnum);
		GL32.glCopyBufferSubData(GlBufferType.COPY_READ_BUFFER.glEnum, type.glEnum, 0, 0, oldSize);

		delete();
		setHandle(newHandle);
	}

	@Override
	public void upload(ByteBuffer directBuffer) {
		bind();
		GL32.glBufferData(type.glEnum, directBuffer, usage.glEnum);
		this.size = directBuffer.capacity();
	}

	@Override
	public MappedBuffer map() {
		bind();
		ByteBuffer byteBuffer = GL30.glMapBufferRange(type.glEnum, 0, size, GL30.GL_MAP_WRITE_BIT);

		if (byteBuffer == null) {
			throw new GlException(GlError.poll(), "Could not map buffer");
		}

		return new MappedBuffer(this, byteBuffer, 0, size);
	}

	@Override
	public boolean isPersistent() {
		return false;
	}
}
