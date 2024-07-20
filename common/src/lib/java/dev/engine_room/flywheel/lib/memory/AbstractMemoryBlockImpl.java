package dev.engine_room.flywheel.lib.memory;

import java.lang.ref.Cleaner;
import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryUtil;

non-sealed abstract class AbstractMemoryBlockImpl implements MemoryBlock {
	static final Cleaner CLEANER = Cleaner.create();

	final long ptr;
	final long size;

	boolean freed;

	AbstractMemoryBlockImpl(long ptr, long size) {
		this.ptr = ptr;
		this.size = size;
	}

	void assertAllocated() {
		if (freed) {
			throw new IllegalStateException("Operation called on freed MemoryBlock!");
		}
	}

	@Override
	public long ptr() {
		assertAllocated();
		return ptr;
	}

	@Override
	public long size() {
		assertAllocated();
		return size;
	}

	@Override
	public boolean isFreed() {
		return freed;
	}

	@Override
	public void copyTo(MemoryBlock block) {
		assertAllocated();
		long bytes = Math.min(size, block.size());
		copyTo(block.ptr(), bytes);
	}

	@Override
	public void copyTo(long ptr, long bytes) {
		assertAllocated();
		MemoryUtil.memCopy(this.ptr, ptr, bytes);
	}

	@Override
	public void copyTo(long ptr) {
		assertAllocated();
		copyTo(ptr, size);
	}

	@Override
	public void clear() {
		assertAllocated();
		MemoryUtil.memSet(ptr, 0, size);
	}

	@Override
	public ByteBuffer asBuffer() {
		assertAllocated();
		int intSize = (int) size;
		if (intSize != size) {
			throw new UnsupportedOperationException("Cannot create buffer with long capacity!");
		}
		return MemoryUtil.memByteBuffer(ptr, intSize);
	}

	void freeInner() {
		FlwMemoryTracker._freeCpuMemory(size);
		freed = true;
	}

	@Override
	public void free() {
		assertAllocated();
		FlwMemoryTracker.free(ptr);
		freeInner();
	}
}
