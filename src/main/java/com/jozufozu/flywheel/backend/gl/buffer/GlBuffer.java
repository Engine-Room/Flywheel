package com.jozufozu.flywheel.backend.gl.buffer;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL20;

import com.jozufozu.flywheel.backend.gl.GlObject;

public abstract class GlBuffer extends GlObject {

	protected final GlBufferType type;

	public GlBuffer(GlBufferType type) {
		_create();
		this.type = type;
	}

	public GlBufferType getBufferTarget() {
		return type;
	}

	public void bind() {
		GL20.glBindBuffer(type.glEnum, handle());
	}

	public void unbind() {
		GL20.glBindBuffer(type.glEnum, 0);
	}

	public void doneForThisFrame() {

	}

	public abstract void alloc(long size);

	public abstract void upload(ByteBuffer directBuffer);

	public abstract MappedBuffer getBuffer(int offset, int length);

	protected void _create() {
		setHandle(GL20.glGenBuffers());
	}

	protected void deleteInternal(int handle) {
		GL20.glDeleteBuffers(handle);
	}
}
