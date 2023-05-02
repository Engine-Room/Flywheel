package com.jozufozu.flywheel.gl.buffer;

import static org.lwjgl.opengl.GL15.glDeleteBuffers;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.gl.GlObject;
import com.jozufozu.flywheel.lib.memory.FlwMemoryTracker;
import com.jozufozu.flywheel.lib.memory.MemoryBlock;
import com.mojang.blaze3d.platform.GlStateManager;

public class GlBuffer extends GlObject {
	public static final Buffer IMPL = new Buffer.DSA().fallback();
	protected final GlBufferUsage usage;
	/**
	 * The size (in bytes) of the buffer on the GPU.
	 */
	protected long size;
	/**
	 * How much extra room to give the buffer when we reallocate.
	 */
	protected int growthMargin;

	public GlBuffer() {
		this(GlBufferUsage.STATIC_DRAW);
	}

	public GlBuffer(GlBufferUsage usage) {
		setHandle(IMPL.create());
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
		IMPL.data(handle(), size, MemoryUtil.NULL, usage.glEnum);
		FlwMemoryTracker._allocGPUMemory(size);
	}

	private void realloc(long capacity) {
		FlwMemoryTracker._freeGPUMemory(size);
		var oldSize = size;
		increaseSize(capacity);

		int oldHandle = handle();
		int newHandle = IMPL.create();
		IMPL.data(newHandle, size, MemoryUtil.NULL, usage.glEnum);
		IMPL.copyData(oldHandle, newHandle, 0, 0, oldSize);
		glDeleteBuffers(oldHandle);
		setHandle(newHandle);

		FlwMemoryTracker._allocGPUMemory(size);
	}

	/**
	 * Increase the size of the buffer to at least the given capacity.
	 */
	private void increaseSize(long capacity) {
		size = capacity + growthMargin;
	}

	public void upload(MemoryBlock directBuffer) {
		FlwMemoryTracker._freeGPUMemory(size);
		IMPL.data(handle(), directBuffer.size(), directBuffer.ptr(), usage.glEnum);
		size = directBuffer.size();
		FlwMemoryTracker._allocGPUMemory(size);
	}

	public MappedBuffer map() {
		return new MappedBuffer(handle(), size);
	}

	public void growthMargin(int growthMargin) {
		this.growthMargin = growthMargin;
	}

	public long size() {
		return size;
	}

	protected void deleteInternal(int handle) {
		GlStateManager._glDeleteBuffers(handle);
		FlwMemoryTracker._freeGPUMemory(size);
	}
}
