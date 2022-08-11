package com.jozufozu.flywheel.backend.memory;

import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryUtil;

sealed class MemoryBlockImpl implements MemoryBlock permits TrackedMemoryBlockImpl {
	final long ptr;
	final long size;

	boolean freed;

	MemoryBlockImpl(long ptr, long size) {
		this.ptr = ptr;
		this.size = size;
	}

	@Override
	public long ptr() {
		return ptr;
	}

	@Override
	public long size() {
		return size;
	}

	@Override
	public boolean isFreed() {
		return freed;
	}

	@Override
	public boolean isTracked() {
		return false;
	}

	@Override
	public void copyTo(long ptr, long bytes) {
		MemoryUtil.memCopy(this.ptr, ptr, bytes);
	}

	@Override
	public void clear() {
		MemoryUtil.memSet(ptr, 0, size);
	}

	@Override
	public ByteBuffer asBuffer() {
		int intSize = (int) size;
		if (intSize != size) {
			throw new UnsupportedOperationException("Cannot create buffer with long capacity!");
		}
		return MemoryUtil.memByteBuffer(ptr, intSize);
	}

	@Override
	public MemoryBlock realloc(long size) {
		MemoryBlock block = FlwMemoryTracker.reallocBlock(this, size);
		freed = true;
		return block;
	}

	@Override
	public MemoryBlock reallocTracked(long size) {
		MemoryBlock block = FlwMemoryTracker.reallocBlockTracked(this, size);
		freed = true;
		return block;
	}

	@Override
	public void free() {
		FlwMemoryTracker.freeBlock(this);
		freed = true;
	}
}
