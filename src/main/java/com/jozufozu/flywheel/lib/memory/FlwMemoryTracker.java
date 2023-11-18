package com.jozufozu.flywheel.lib.memory;

import java.lang.ref.Cleaner;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.lib.util.StringUtil;

public final class FlwMemoryTracker {
	public static final boolean DEBUG_MEMORY_SAFETY = System.getProperty("flw.debugMemorySafety") != null;

	static final Cleaner CLEANER = Cleaner.create();

	// TODO: Should these be volatile?
	private static long cpuMemory = 0;
	private static long gpuMemory = 0;

	private FlwMemoryTracker() {
	}

	public static long malloc(long size) {
		long ptr = MemoryUtil.nmemAlloc(size);
		if (ptr == MemoryUtil.NULL) {
			throw new OutOfMemoryError("Failed to allocate " + size + " bytes");
		}
		return ptr;
	}

	public static long calloc(long num, long size) {
		long ptr = MemoryUtil.nmemCalloc(num, size);
		if (ptr == MemoryUtil.NULL) {
			throw new OutOfMemoryError("Failed to allocate " + num + " elements of size " + size + " bytes");
		}
		return ptr;
	}

	public static long realloc(long ptr, long size) {
		ptr = MemoryUtil.nmemRealloc(ptr, size);
		if (ptr == MemoryUtil.NULL) {
			throw new OutOfMemoryError("Failed to reallocate " + size + " bytes for address " + StringUtil.formatAddress(ptr));
		}
		return ptr;
	}

	public static void free(long ptr) {
		MemoryUtil.nmemFree(ptr);
	}

	public static void _allocCPUMemory(long size) {
		cpuMemory += size;
	}

	public static void _freeCPUMemory(long size) {
		cpuMemory -= size;
	}

	public static void _allocGPUMemory(long size) {
		gpuMemory += size;
	}

	public static void _freeGPUMemory(long size) {
		gpuMemory -= size;
	}

	public static long getCPUMemory() {
		return cpuMemory;
	}

	public static long getGPUMemory() {
		return gpuMemory;
	}
}
