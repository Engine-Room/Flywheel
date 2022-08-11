package com.jozufozu.flywheel.backend.memory;

import java.lang.ref.Cleaner;
import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryUtil;

public class FlwMemoryTracker {
	private static final Cleaner CLEANER = Cleaner.create();

	private static long cpuMemory = 0;
	private static long gpuMemory = 0;

	public static long malloc(long size) {
		long ptr = MemoryUtil.nmemAlloc(size);
		if (ptr == MemoryUtil.NULL) {
			throw new OutOfMemoryError("Failed to allocate " + size + " bytes");
		}
		return ptr;
	}

	public static MemoryBlock mallocBlock(long size) {
		MemoryBlock block = new MemoryBlockImpl(malloc(size), size);
		cpuMemory += block.size();
		return block;
	}

	public static MemoryBlock mallocBlockTracked(long size) {
		TrackedMemoryBlockImpl block = new TrackedMemoryBlockImpl(malloc(size), size);
		block.cleanable = CLEANER.register(block, () -> {
			if (!block.isFreed()) {
				block.free();
			}
		});
		cpuMemory += block.size();
		return block;
	}

	@Deprecated
	public static ByteBuffer mallocBuffer(int size) {
		ByteBuffer buffer = MemoryUtil.memByteBuffer(malloc(size), size);
		cpuMemory += buffer.capacity();
		return buffer;
	}

	public static long calloc(long num, long size) {
		long ptr = MemoryUtil.nmemCalloc(num, size);
		if (ptr == MemoryUtil.NULL) {
			throw new OutOfMemoryError("Failed to allocate " + num + " blocks of size " + size + " bytes");
		}
		return ptr;
	}

	public static MemoryBlock callocBlock(long num, long size) {
		MemoryBlock block = new MemoryBlockImpl(calloc(num, size), num * size);
		cpuMemory += block.size();
		return block;
	}

	public static MemoryBlock callocBlockTracked(long num, long size) {
		TrackedMemoryBlockImpl block = new TrackedMemoryBlockImpl(calloc(num, size), num * size);
		block.cleanable = CLEANER.register(block, () -> {
			if (!block.isFreed()) {
				block.free();
			}
		});
		cpuMemory += block.size();
		return block;
	}

	public static long realloc(long ptr, long size) {
		ptr = MemoryUtil.nmemRealloc(ptr, size);
		if (ptr == MemoryUtil.NULL) {
			throw new OutOfMemoryError("Failed to reallocate " + size + " bytes for address 0x" + Long.toHexString(ptr));
		}
		return ptr;
	}

	public static MemoryBlock reallocBlock(MemoryBlock block, long size) {
		MemoryBlock newBlock = new MemoryBlockImpl(realloc(block.ptr(), size), size);
		cpuMemory += -block.size() + newBlock.size();
		return newBlock;
	}

	public static MemoryBlock reallocBlockTracked(MemoryBlock block, long size) {
		TrackedMemoryBlockImpl newBlock = new TrackedMemoryBlockImpl(realloc(block.ptr(), size), size);
		newBlock.cleanable = CLEANER.register(newBlock, () -> {
			if (!newBlock.isFreed()) {
				newBlock.free();
			}
		});
		cpuMemory += -block.size() + newBlock.size();
		return newBlock;
	}

	@Deprecated
	public static ByteBuffer reallocBuffer(ByteBuffer buffer, int size) {
		ByteBuffer newBuffer = MemoryUtil.memByteBuffer(realloc(MemoryUtil.memAddress(buffer), size), size);
		cpuMemory += -buffer.capacity() + newBuffer.capacity();
		return newBuffer;
	}

	public static void free(long ptr) {
		MemoryUtil.nmemFree(ptr);
	}

	public static void freeBlock(MemoryBlock block) {
		free(block.ptr());
		cpuMemory -= block.size();
	}

	@Deprecated
	public static void freeBuffer(ByteBuffer buffer) {
		free(MemoryUtil.memAddress(buffer));
		cpuMemory -= buffer.capacity();
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
