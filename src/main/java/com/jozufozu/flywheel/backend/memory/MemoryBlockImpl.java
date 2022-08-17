package com.jozufozu.flywheel.backend.memory;

import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryUtil;

non-sealed class MemoryBlockImpl implements MemoryBlock {
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
	public void copyTo(long ptr) {
		copyTo(ptr, size);
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

	void freeInner() {
		FlwMemoryTracker._freeCPUMemory(size);
		freed = true;
	}

	@Override
	public MemoryBlock realloc(long size) {
		MemoryBlock block = new MemoryBlockImpl(FlwMemoryTracker.realloc(ptr, size), size);
		FlwMemoryTracker._allocCPUMemory(block.size());
		freeInner();
		return block;
	}

	@Override
	public MemoryBlock reallocTracked(long size) {
		MemoryBlock block = new TrackedMemoryBlockImpl(FlwMemoryTracker.realloc(ptr, size), size, FlwMemoryTracker.CLEANER);
		FlwMemoryTracker._allocCPUMemory(block.size());
		freeInner();
		return block;
	}

	@Override
	public void free() {
		FlwMemoryTracker.free(ptr);
		freeInner();
	}

	static MemoryBlock malloc(long size) {
		MemoryBlock block = new MemoryBlockImpl(FlwMemoryTracker.malloc(size), size);
		FlwMemoryTracker._allocCPUMemory(block.size());
		return block;
	}

	static MemoryBlock calloc(long num, long size) {
		MemoryBlock block = new MemoryBlockImpl(FlwMemoryTracker.calloc(num, size), num * size);
		FlwMemoryTracker._allocCPUMemory(block.size());
		return block;
	}
}
