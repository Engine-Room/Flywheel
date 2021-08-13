package com.jozufozu.flywheel.backend.gl.buffer;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.versioned.MapBufferRange;

public class GlBufferImpl extends GlBuffer {

	protected final GlBufferUsage usage;

	public GlBufferImpl(GlBufferType type) {
		this(type, GlBufferUsage.STATIC_DRAW);
	}

	public GlBufferImpl(GlBufferType type, GlBufferUsage usage) {
		super(type);
		this.usage = usage;
	}

	public void alloc(long size) {
		GL15.glBufferData(type.glEnum, size, usage.glEnum);
	}

	public void upload(ByteBuffer directBuffer) {
		GL15.glBufferData(type.glEnum, directBuffer, usage.glEnum);
	}

	public MappedBuffer getBuffer(int offset, int length) {
		if (Backend.getInstance().compat.mapBufferRange != MapBufferRange.UNSUPPORTED) {
			return new MappedBufferRange(this, offset, length, GL30.GL_MAP_WRITE_BIT);
		} else {
			MappedFullBuffer fullBuffer = new MappedFullBuffer(this, MappedBufferUsage.WRITE_ONLY);
			fullBuffer.position(offset);
			return fullBuffer;
		}
	}
}
