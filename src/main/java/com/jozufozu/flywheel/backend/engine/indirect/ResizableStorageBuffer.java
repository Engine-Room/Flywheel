package com.jozufozu.flywheel.backend.engine.indirect;

import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL45.glCopyNamedBufferSubData;
import static org.lwjgl.opengl.GL45.glCreateBuffers;
import static org.lwjgl.opengl.GL45.glNamedBufferStorage;

import com.jozufozu.flywheel.gl.GlObject;
import com.jozufozu.flywheel.lib.memory.FlwMemoryTracker;

/**
 * A buffer for storing data on the GPU that can be resized.
 * <br>
 * The only way to get data in and out is to use GPU copies.
 */
public class ResizableStorageBuffer extends GlObject {
	private long capacity = 0;

	public ResizableStorageBuffer() {
		handle(glCreateBuffers());
	}

	public long capacity() {
		return capacity;
	}

	public void ensureCapacity(long capacity) {
		FlwMemoryTracker._freeGPUMemory(this.capacity);

		if (this.capacity > 0) {
			int oldHandle = handle();
			int newHandle = glCreateBuffers();

			glNamedBufferStorage(newHandle, capacity, 0);

			glCopyNamedBufferSubData(oldHandle, newHandle, 0, 0, this.capacity);

			deleteInternal(oldHandle);

			handle(newHandle);
		} else {
			glNamedBufferStorage(handle(), capacity, 0);
		}
		this.capacity = capacity;
		FlwMemoryTracker._allocGPUMemory(this.capacity);
	}

	@Override
	protected void deleteInternal(int handle) {
		glDeleteBuffers(handle);
	}

	@Override
	public void delete() {
		super.delete();
		FlwMemoryTracker._freeGPUMemory(capacity);
	}
}
