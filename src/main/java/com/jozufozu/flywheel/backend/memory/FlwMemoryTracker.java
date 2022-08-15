package com.jozufozu.flywheel.backend.memory;

import java.lang.ref.Cleaner;
import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryUtil;

public class FlwMemoryTracker {
	static final Cleaner CLEANER = Cleaner.create();

	private static long cpuMemory = 0;
	private static long gpuMemory = 0;

	public static long malloc(long size) {
		long ptr = MemoryUtil.nmemAlloc(size);
		if (ptr == MemoryUtil.NULL) {
			throw new OutOfMemoryError("Failed to allocate " + size + " bytes");
		}
		return ptr;
	}

	/**
	 * @deprecated Use {@link MemoryBlock#malloc(long)} or {@link MemoryBlock#mallocTracked(long)} and
	 * {@link MemoryBlock#asBuffer()} instead. This method should only be used if specifically a {@linkplain ByteBuffer} is needed and it is
	 * short-lived.
	 */
	@Deprecated
	public static ByteBuffer mallocBuffer(int size) {
		ByteBuffer buffer = MemoryUtil.memByteBuffer(malloc(size), size);
		_allocCPUMemory(buffer.capacity());
		return buffer;
	}

	public static long calloc(long num, long size) {
		long ptr = MemoryUtil.nmemCalloc(num, size);
		if (ptr == MemoryUtil.NULL) {
			throw new OutOfMemoryError("Failed to allocate " + num + " elements of size " + size + " bytes");
		}
		return ptr;
	}

	/**
	 * @deprecated Use {@link MemoryBlock#calloc(long, long)} or {@link MemoryBlock#callocTracked(long, long)} and
	 * {@link MemoryBlock#asBuffer()} instead. This method should only be used if specifically a {@linkplain ByteBuffer} is needed and it is
	 * short-lived.
	 */
	@Deprecated
	public static ByteBuffer callocBuffer(int num, int size) {
		ByteBuffer buffer = MemoryUtil.memByteBuffer(calloc(num, size), num * size);
		_allocCPUMemory(buffer.capacity());
		return buffer;
	}

	public static long realloc(long ptr, long size) {
		ptr = MemoryUtil.nmemRealloc(ptr, size);
		if (ptr == MemoryUtil.NULL) {
			throw new OutOfMemoryError("Failed to reallocate " + size + " bytes for address 0x" + Long.toHexString(ptr));
		}
		return ptr;
	}

	/**
	 * @deprecated Use {@link MemoryBlock#realloc(long)} or {@link MemoryBlock#reallocTracked(long)} instead. This method
	 * should only be used if specifically a {@linkplain ByteBuffer} is needed and it is short-lived.
	 */
	@Deprecated
	public static ByteBuffer reallocBuffer(ByteBuffer buffer, int size) {
		ByteBuffer newBuffer = MemoryUtil.memByteBuffer(realloc(MemoryUtil.memAddress(buffer), size), size);
		_freeCPUMemory(buffer.capacity());
		_allocCPUMemory(newBuffer.capacity());
		return newBuffer;
	}

	public static void free(long ptr) {
		MemoryUtil.nmemFree(ptr);
	}

	/**
	 * @deprecated Use {@link MemoryBlock#free} instead. This method should only be used if specifically a {@linkplain ByteBuffer} is needed and
	 * it is short-lived.
	 */
	@Deprecated
	public static void freeBuffer(ByteBuffer buffer) {
		free(MemoryUtil.memAddress(buffer));
		_freeCPUMemory(buffer.capacity());
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
