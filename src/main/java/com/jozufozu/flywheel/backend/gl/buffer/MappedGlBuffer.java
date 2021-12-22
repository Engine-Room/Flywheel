package com.jozufozu.flywheel.backend.gl.buffer;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import com.jozufozu.flywheel.backend.gl.error.GlError;
import com.jozufozu.flywheel.backend.gl.error.GlException;

public class MappedGlBuffer extends GlBuffer implements Mappable {

	protected final GlBufferUsage usage;

	public MappedGlBuffer(GlBufferType type) {
		this(type, GlBufferUsage.STATIC_DRAW);
	}

	public MappedGlBuffer(GlBufferType type, GlBufferUsage usage) {
		super(type);
		this.usage = usage;
	}

	protected void alloc(long size) {
		GL15.glBufferData(type.glEnum, size, usage.glEnum);
	}

	public void upload(ByteBuffer directBuffer) {
		GL15.glBufferData(type.glEnum, directBuffer, usage.glEnum);
	}

	public MappedBuffer getBuffer(long offset, long length) {
		ByteBuffer byteBuffer = GL30.glMapBufferRange(type.glEnum, offset, length, GL30.GL_MAP_WRITE_BIT);

		if (byteBuffer == null) {
			throw new GlException(GlError.poll(), "Could not map buffer");
		}

		return new MappedBuffer(this, byteBuffer, offset, length);
	}

	@Override
	public GlBufferType getType() {
		return type;
	}

	@Override
	public boolean isPersistent() {
		return false;
	}
}
