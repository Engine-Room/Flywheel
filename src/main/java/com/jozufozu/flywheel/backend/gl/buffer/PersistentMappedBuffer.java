package com.jozufozu.flywheel.backend.gl.buffer;

import java.nio.ByteBuffer;

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

		ByteBuffer byteBuffer = Backend.getInstance().compat.mapBufferRange.mapBuffer(owner.type, offset, length, owner.flags);

		GlError error = GlError.poll();

		if (error != null) {
			throw new GlException(error, StringUtil.args("mapBuffer", owner.type, offset, length, owner.flags));
		}

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
