package com.jozufozu.flywheel.backend.gl.buffer;

import static org.lwjgl.opengl.GL44.*;

import java.nio.ByteBuffer;


public class PersistentGlBuffer extends GlBuffer {

	private PersistentMappedBuffer buffer;
	int flags;

	long size;
	private long fence = -1;

	public PersistentGlBuffer(GlBufferType type) {
		super(type);

		flags = GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT;
	}

	@Override
	public void doneForThisFrame() {
		fence = glFenceSync(GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
	}

	@Override
	public void alloc(long size) {
		this.size = size;

		if (buffer != null) {
			deleteInternal(handle());
			_create();
		}

		glBufferStorage(type.glEnum, size, flags);

		buffer = new PersistentMappedBuffer(this);
	}

	@Override
	public void upload(ByteBuffer directBuffer) {

	}

	@Override
	public MappedBuffer getBuffer(int offset, int length) {

		if (fence != -1) {
			int waitReturn = GL_UNSIGNALED;
			while (waitReturn != GL_ALREADY_SIGNALED && waitReturn != GL_CONDITION_SATISFIED) {
				waitReturn = glClientWaitSync(fence, GL_SYNC_FLUSH_COMMANDS_BIT, 1);
			}

			glDeleteSync(fence);
		}

		fence = -1;

		buffer.position(offset);

		return buffer;
	}
}
