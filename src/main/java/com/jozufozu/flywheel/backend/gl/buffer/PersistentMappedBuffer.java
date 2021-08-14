package com.jozufozu.flywheel.backend.gl.buffer;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL44;
import org.lwjgl.opengl.GL46;

import com.jozufozu.flywheel.backend.Backend;

public class PersistentMappedBuffer extends MappedBuffer {

	PersistentGlBuffer owner;

	public PersistentMappedBuffer(PersistentGlBuffer buffer) {
		super(buffer);
		owner = buffer;

		ByteBuffer byteBuffer = Backend.getInstance().compat.mapBufferRange.mapBuffer(owner.type, 0, owner.size, owner.flags);

		setInternal(byteBuffer);
	}

	@Override
	public void flush() {

	}

	@Override
	protected void checkAndMap() {

	}
}
