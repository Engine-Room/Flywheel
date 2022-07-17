package com.jozufozu.flywheel.backend.gl.buffer;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL15;

public class MappedBuffer extends VecBuffer implements AutoCloseable {

	protected final long offset;
	protected final long length;
	protected final Mappable owner;

	public MappedBuffer(Mappable owner, ByteBuffer internal, long offset, long length) {
		this.internal = internal;
		this.owner = owner;
		this.offset = offset;
		this.length = length;
	}

	/**
	 * Make the changes in client memory available to the GPU.
	 */
	public void flush() {
		if (owner.isPersistent()) return;

		if (internal == null) return;

		GL15.glUnmapBuffer(owner.getType().glEnum);
		internal = null;
	}

	@Override
	public MappedBuffer position(int p) {
		if (p < offset || p >= offset + length) {
			throw new IndexOutOfBoundsException("Index " + p + " is not mapped");
		}
		super.position(p - (int) offset);
		return this;
	}

	@Override
	public void close() {
		flush();
	}
}
