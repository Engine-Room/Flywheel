package com.jozufozu.flywheel.backend.gl.buffer;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL15;
import org.lwjgl.system.MemoryUtil;

public class MappedBuffer implements AutoCloseable {

	private final long offset;
	private final long length;
	private final GlBuffer owner;
	private final boolean persistent;
	private ByteBuffer internal;

	public MappedBuffer(GlBuffer owner, ByteBuffer internal, long offset, long length) {
		this.internal = internal;
		this.owner = owner;
		this.offset = offset;
		this.length = length;
		persistent = owner.isPersistent();
	}

	/**
	 * Make the changes in client memory available to the GPU.
	 */
	public void flush() {
		if (persistent) return;

		if (internal == null) return;

		owner.bind();
		GL15.glUnmapBuffer(owner.getType().glEnum);
		internal = null;
	}

	public MappedBuffer position(int p) {
		if (p < offset || p >= offset + length) {
			throw new IndexOutOfBoundsException("Index " + p + " is not mapped");
		}
		internal.position(p - (int) offset);
		return this;
	}

	@Override
	public void close() {
		flush();
	}

	public ByteBuffer unwrap() {
		return internal;
	}

	public long getMemAddress() {
		return MemoryUtil.memAddress(internal);
	}

	public void clear(long clearStart, long clearLength) {
		if (clearLength <= 0) {
			return;
		}

		if (clearStart < offset || clearStart + clearLength > offset + length) {
			throw new IndexOutOfBoundsException("Clear range [" + clearStart + "," + (clearStart + clearLength) + "] is not mapped");
		}

		long addr = MemoryUtil.memAddress(unwrap()) + clearStart;

		MemoryUtil.memSet(addr, 0, clearLength);
	}
}
