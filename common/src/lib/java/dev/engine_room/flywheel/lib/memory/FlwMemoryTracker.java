package dev.engine_room.flywheel.lib.memory;

import java.util.concurrent.atomic.AtomicLong;

import org.lwjgl.system.MemoryUtil;

import dev.engine_room.flywheel.lib.util.StringUtil;

public final class FlwMemoryTracker {
	public static final boolean DEBUG_MEMORY_SAFETY = System.getProperty("flw.debugMemorySafety") != null;

	private static final AtomicLong CPU_MEMORY = new AtomicLong(0);
	private static final AtomicLong GPU_MEMORY = new AtomicLong(0);

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
		CPU_MEMORY.getAndAdd(size);
	}

	public static void _freeCPUMemory(long size) {
		CPU_MEMORY.getAndAdd(-size);
	}

	public static void _allocGPUMemory(long size) {
		GPU_MEMORY.getAndAdd(size);
	}

	public static void _freeGPUMemory(long size) {
		GPU_MEMORY.getAndAdd(-size);
	}

	public static long getCPUMemory() {
		return CPU_MEMORY.get();
	}

	public static long getGPUMemory() {
		return GPU_MEMORY.get();
	}
}
