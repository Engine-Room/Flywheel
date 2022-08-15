package com.jozufozu.flywheel.backend.gl.buffer;

import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL15.nglBufferData;
import static org.lwjgl.opengl.GL30.GL_MAP_WRITE_BIT;
import static org.lwjgl.opengl.GL30.nglMapBufferRange;
import static org.lwjgl.opengl.GL31.glCopyBufferSubData;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.backend.gl.GlObject;
import com.jozufozu.flywheel.backend.gl.error.GlError;
import com.jozufozu.flywheel.backend.gl.error.GlException;
import com.jozufozu.flywheel.backend.memory.FlwMemoryTracker;
import com.jozufozu.flywheel.backend.memory.MemoryBlock;

public class GlBuffer extends GlObject {

	public final GlBufferType type;
	protected final GlBufferUsage usage;
	/**
	 * The size (in bytes) of the buffer on the GPU.
	 */
	protected long size;
	/**
	 * How much extra room to give the buffer when we reallocate.
	 */
	protected int growthMargin;

	public GlBuffer(GlBufferType type) {
		this(type, GlBufferUsage.STATIC_DRAW);
	}

	public GlBuffer(GlBufferType type, GlBufferUsage usage) {
		setHandle(glGenBuffers());
		this.type = type;
		this.usage = usage;
	}

	public boolean ensureCapacity(long size) {
		if (size < 0) {
			throw new IllegalArgumentException("Size " + size + " < 0");
		}

		if (size == 0) {
			return false;
		}

		if (this.size == 0) {
			this.size = size;
			bind();
			glBufferData(type.glEnum, size, usage.glEnum);
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
		type.bind(newHandle);

		glBufferData(type.glEnum, newSize, usage.glEnum);
		glCopyBufferSubData(GlBufferType.COPY_READ_BUFFER.glEnum, type.glEnum, 0, 0, oldSize);

		glDeleteBuffers(oldHandle);
		setHandle(newHandle);
	}

	public void upload(MemoryBlock directBuffer) {
		bind();
		FlwMemoryTracker._freeGPUMemory(size);
		nglBufferData(type.glEnum, directBuffer.size(), directBuffer.ptr(), usage.glEnum);
		this.size = directBuffer.size();
		FlwMemoryTracker._allocGPUMemory(size);
	}

	public MappedBuffer map() {
		bind();
		long ptr = nglMapBufferRange(type.glEnum, 0, size, GL_MAP_WRITE_BIT);

		if (ptr == MemoryUtil.NULL) {
			throw new GlException(GlError.poll(), "Could not map buffer");
		}

		return new MappedBuffer(this, ptr, 0, size);
	}

	public boolean isPersistent() {
		return false;
	}

	public void setGrowthMargin(int growthMargin) {
		this.growthMargin = growthMargin;
	}

	public long getSize() {
		return size;
	}

	public GlBufferType getType() {
		return type;
	}

	public void bind() {
		type.bind(handle());
	}

	public void unbind() {
		type.unbind();
	}

	protected void deleteInternal(int handle) {
		glDeleteBuffers(handle);
		FlwMemoryTracker._freeGPUMemory(size);
	}
}
