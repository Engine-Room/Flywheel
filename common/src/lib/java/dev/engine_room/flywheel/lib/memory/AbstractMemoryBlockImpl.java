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
	public void copyTo(MemoryBlock block) {
		long bytes = Math.min(size, block.size());
		copyTo(block.ptr(), bytes);
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
	public void free() {
		FlwMemoryTracker.free(ptr);
		freeInner();
	}
}
