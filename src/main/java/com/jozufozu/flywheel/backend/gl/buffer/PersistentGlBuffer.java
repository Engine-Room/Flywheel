package com.jozufozu.flywheel.backend.gl.buffer;

import static org.lwjgl.opengl.GL44.*;

import java.nio.ByteBuffer;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.GlFence;


public class PersistentGlBuffer extends GlBuffer {

	private PersistentMappedBuffer buffer;
	int flags;

	long size;
	GlFence fence;

	public PersistentGlBuffer(GlBufferType type) {
		super(type);

		flags = GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT;
		fence = new GlFence();
	}

	@Override
	public void doneForThisFrame() {
		fence.post();
	}

	@Override
	public void alloc(long size) {
		this.size = size;

		if (buffer != null) {
			deleteInternal(handle());
			_create();
		}

		fence.clear();

		Backend.getInstance().compat.bufferStorage.bufferStorage(type.glEnum, size, flags);

		buffer = new PersistentMappedBuffer(this);
	}

	@Override
	public void upload(ByteBuffer directBuffer) {

	}

	@Override
	public MappedBuffer getBuffer(int offset, int length) {

		fence.waitSync();

		buffer.position(offset);

		return buffer;
	}
}
