package com.jozufozu.flywheel.gl.buffer;

import static org.lwjgl.system.MemoryUtil.NULL;

import org.lwjgl.opengl.GL15;
import org.lwjgl.system.MemoryUtil;

public class MappedBuffer implements AutoCloseable {

	private final long offset;
	private final long length;
	private final GlBuffer owner;
	private final boolean persistent;
	private long ptr;

	public MappedBuffer(GlBuffer owner, long ptr, long offset, long length) {
		this.ptr = ptr;
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

		if (ptr == NULL) return;

		owner.bind();
		GL15.glUnmapBuffer(owner.getType().glEnum);
		ptr = NULL;
	}

	@Override
	public void close() {
		flush();
	}

	public long getPtr() {
		return ptr;
	}

	public void clear(long clearStart, long clearLength) {
		if (clearLength <= 0) {
			return;
		}

		if (clearStart < offset || clearStart + clearLength > offset + length) {
			throw new IndexOutOfBoundsException("Clear range [" + clearStart + "," + (clearStart + clearLength) + "] is not mapped");
		}

		long addr = ptr + clearStart;

		MemoryUtil.memSet(addr, 0, clearLength);
	}
}
