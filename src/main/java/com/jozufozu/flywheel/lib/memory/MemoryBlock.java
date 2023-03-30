package com.jozufozu.flywheel.lib.memory;

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
		if (FlwMemoryTracker.DEBUG_MEMORY_SAFETY) {
			return DebugMemoryBlockImpl.malloc(size);
		} else {
			return MemoryBlockImpl.malloc(size);
		}
	}

	static MemoryBlock mallocTracked(long size) {
		return TrackedMemoryBlockImpl.malloc(size);
	}

	static MemoryBlock calloc(long num, long size) {
		if (FlwMemoryTracker.DEBUG_MEMORY_SAFETY) {
			return DebugMemoryBlockImpl.calloc(num, size);
		} else {
			return MemoryBlockImpl.calloc(num, size);
		}
	}

	static MemoryBlock callocTracked(long num, long size) {
		return TrackedMemoryBlockImpl.calloc(num, size);
	}
}
