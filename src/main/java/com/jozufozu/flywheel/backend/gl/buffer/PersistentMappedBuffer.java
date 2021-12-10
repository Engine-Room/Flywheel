package com.jozufozu.flywheel.backend.gl.buffer;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL30;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.error.GlError;
import com.jozufozu.flywheel.backend.gl.error.GlException;
import com.jozufozu.flywheel.util.StringUtil;

public class PersistentMappedBuffer extends MappedBuffer {

	private final long offset;
	private final long length;
	PersistentGlBuffer owner;

	public PersistentMappedBuffer(PersistentGlBuffer buffer) {
		super(buffer);
		owner = buffer;
		offset = 0;
		length = owner.size;

		ByteBuffer byteBuffer = GL30.glMapBufferRange(owner.type.glEnum, offset, length, owner.flags);

		GlError.pollAndThrow(() -> StringUtil.args("mapBuffer", owner.type, offset, length, owner.flags));

		setInternal(byteBuffer);
	}

	@Override
	public MappedBuffer position(int p) {
		if (p < offset || p >= offset + length) {
			throw new IndexOutOfBoundsException("Index " + p + " is not mapped");
		}
		return super.position(p - (int) offset);
	}

	@Override
	public void flush() {

	}

	@Override
	protected void checkAndMap() {

	}
}
