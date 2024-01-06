package com.jozufozu.flywheel.backend.gl.buffer;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.backend.gl.GlObject;
import com.jozufozu.flywheel.lib.memory.FlwMemoryTracker;
import com.jozufozu.flywheel.lib.memory.MemoryBlock;
import com.mojang.blaze3d.platform.GlStateManager;

import it.unimi.dsi.fastutil.longs.LongUnaryOperator;

public class GlBuffer extends GlObject {
	protected final GlBufferUsage usage;
	/**
	 * The size (in bytes) of the buffer on the GPU.
	 */
	protected long size;
	/**
	 * A mapping to adjust the size of the buffer when allocating.
	 */
	protected LongUnaryOperator growthFunction = LongUnaryOperator.identity();

	public GlBuffer() {
		this(GlBufferUsage.STATIC_DRAW);
	}

	public GlBuffer(GlBufferUsage usage) {
		handle(Buffer.IMPL.create());
		this.usage = usage;
	}

	/**
	 * @return true if the buffer was recreated.
	 */
	public boolean ensureCapacity(long capacity) {
		if (capacity < 0) {
			throw new IllegalArgumentException("Size " + capacity + " < 0");
		}

		if (capacity == 0) {
			return false;
		}

		if (size == 0) {
			alloc(capacity);
			return true;
		}

		if (capacity > size) {
			realloc(capacity);
			return true;
		}

		return false;
	}

	private void alloc(long capacity) {
		increaseSize(capacity);
		Buffer.IMPL.data(handle(), size, MemoryUtil.NULL, usage.glEnum);
		FlwMemoryTracker._allocGPUMemory(size);
	}

	private void realloc(long capacity) {
		FlwMemoryTracker._freeGPUMemory(size);
		var oldSize = size;
		increaseSize(capacity);

		int oldHandle = handle();
		int newHandle = Buffer.IMPL.create();
		Buffer.IMPL.data(newHandle, size, MemoryUtil.NULL, usage.glEnum);
		Buffer.IMPL.copyData(oldHandle, newHandle, 0, 0, oldSize);
		GlStateManager._glDeleteBuffers(oldHandle);
		handle(newHandle);

		FlwMemoryTracker._allocGPUMemory(size);
	}

	/**
	 * Increase the size of the buffer to at least the given capacity.
	 */
	private void increaseSize(long capacity) {
		size = growthFunction.apply(capacity);
	}

	public void upload(MemoryBlock memoryBlock) {
		upload(memoryBlock.ptr(), memoryBlock.size());
	}

	public void upload(long ptr, long size) {
		FlwMemoryTracker._freeGPUMemory(this.size);
		Buffer.IMPL.data(handle(), size, ptr, usage.glEnum);
		this.size = size;
		FlwMemoryTracker._allocGPUMemory(this.size);
	}

	public MappedBuffer map() {
		return new MappedBuffer(handle(), size);
	}

	public void growthFunction(LongUnaryOperator growthFunction) {
		this.growthFunction = growthFunction;
	}

	public long size() {
		return size;
	}

	protected void deleteInternal(int handle) {
		GlStateManager._glDeleteBuffers(handle);
		FlwMemoryTracker._freeGPUMemory(size);
	}
}
