package com.jozufozu.flywheel.backend.memory;

import java.nio.ByteBuffer;

public sealed interface MemoryBlock permits MemoryBlockImpl {
	long ptr();

	long size();

	boolean isFreed();

	boolean isTracked();

	void copyTo(long ptr, long bytes);

	void copyTo(long ptr);

	void clear();

	ByteBuffer asBuffer();

	MemoryBlock realloc(long size);

	MemoryBlock reallocTracked(long size);

	void free();

	static MemoryBlock malloc(long size) {
		return MemoryBlockImpl.mallocBlock(size);
	}

	static MemoryBlock mallocTracked(long size) {
		return TrackedMemoryBlockImpl.mallocBlockTracked(size);
	}

	static MemoryBlock calloc(long num, long size) {
		return MemoryBlockImpl.callocBlock(num, size);
	}

	static MemoryBlock callocTracked(long num, long size) {
		return TrackedMemoryBlockImpl.callocBlockTracked(num, size);
	}
}
