package com.jozufozu.flywheel.backend.memory;

import java.nio.ByteBuffer;

public sealed interface MemoryBlock permits MemoryBlockImpl {
	long ptr();

	long size();

	boolean isFreed();

	boolean isTracked();

	void copyTo(long ptr, long bytes);

	default void copyTo(long ptr) {
		copyTo(ptr, size());
	}

	void clear();

	ByteBuffer asBuffer();

	MemoryBlock realloc(long size);

	MemoryBlock reallocTracked(long size);

	void free();

	static MemoryBlock malloc(long size) {
		return FlwMemoryTracker.mallocBlock(size);
	}

	static MemoryBlock calloc(long num, long size) {
		return FlwMemoryTracker.callocBlock(num, size);
	}

	static MemoryBlock mallocTracked(long size) {
		return FlwMemoryTracker.mallocBlockTracked(size);
	}

	static MemoryBlock callocTracked(long num, long size) {
		return FlwMemoryTracker.callocBlockTracked(num, size);
	}
}
