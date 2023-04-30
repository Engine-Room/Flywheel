package com.jozufozu.flywheel.gl.buffer;

import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL15.nglBufferData;
import static org.lwjgl.opengl.GL31.glCopyBufferSubData;

import com.jozufozu.flywheel.gl.GlObject;
import com.jozufozu.flywheel.lib.memory.FlwMemoryTracker;
import com.jozufozu.flywheel.lib.memory.MemoryBlock;

public class GlBuffer extends GlObject {
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
		setHandle(glGenBuffers());
		this.usage = usage;
	}

	/**
	 * @return true if the buffer was recreated.
	 */
	public boolean ensureCapacity(long size) {
		if (size < 0) {
			throw new IllegalArgumentException("Size " + size + " < 0");
		}

		if (size == 0) {
			return false;
		}

		if (this.size == 0) {
			this.size = size;
			GlBufferType.COPY_WRITE_BUFFER.bind(handle());
			glBufferData(GlBufferType.COPY_WRITE_BUFFER.glEnum, size, usage.glEnum);
			FlwMemoryTracker._allocGPUMemory(size);

			return true;
		}

		if (size > this.size) {
			var oldSize = this.size;
			this.size = size + growthMargin;

			realloc(oldSize, this.size);

			return true;
		}

		return false;
	}

	private void realloc(long oldSize, long newSize) {
		FlwMemoryTracker._freeGPUMemory(oldSize);
		FlwMemoryTracker._allocGPUMemory(newSize);
		var oldHandle = handle();
		var newHandle = glGenBuffers();

		GlBufferType.COPY_READ_BUFFER.bind(oldHandle);
		GlBufferType.COPY_WRITE_BUFFER.bind(newHandle);

		glBufferData(GlBufferType.COPY_WRITE_BUFFER.glEnum, newSize, usage.glEnum);
		glCopyBufferSubData(GlBufferType.COPY_READ_BUFFER.glEnum, GlBufferType.COPY_WRITE_BUFFER.glEnum, 0, 0, oldSize);

		glDeleteBuffers(oldHandle);
		setHandle(newHandle);
	}

	public void upload(MemoryBlock directBuffer) {
		FlwMemoryTracker._freeGPUMemory(size);
		GlBufferType.COPY_WRITE_BUFFER.bind(handle());
		nglBufferData(GlBufferType.COPY_WRITE_BUFFER.glEnum, directBuffer.size(), directBuffer.ptr(), usage.glEnum);
		this.size = directBuffer.size();
		FlwMemoryTracker._allocGPUMemory(size);
	}

	public MappedBuffer map() {
		return new MappedBuffer(handle(), size);
	}

	public void setGrowthMargin(int growthMargin) {
		this.growthMargin = growthMargin;
	}

	public long getSize() {
		return size;
	}

	protected void deleteInternal(int handle) {
		glDeleteBuffers(handle);
		FlwMemoryTracker._freeGPUMemory(size);
	}
}
