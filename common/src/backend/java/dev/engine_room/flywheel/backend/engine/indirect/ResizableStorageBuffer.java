package dev.engine_room.flywheel.backend.engine.indirect;

import org.lwjgl.opengl.GL45;

import com.mojang.blaze3d.opengl.GlStateManager;

import dev.engine_room.flywheel.backend.gl.GlObject;
import dev.engine_room.flywheel.lib.memory.FlwMemoryTracker;

/**
 * A buffer for storing data on the GPU that can be resized.
 * <br>
 * The only way to get data in and out is to use GPU copies.
 */
public class ResizableStorageBuffer extends GlObject {
	private long capacity = 0;

	public ResizableStorageBuffer() {
		handle(GL45.glCreateBuffers());
	}

	public long capacity() {
		return capacity;
	}

	public void ensureCapacity(long capacity) {
		FlwMemoryTracker._freeGpuMemory(this.capacity);

		if (this.capacity > 0) {
			int oldHandle = handle();
			int newHandle = GL45.glCreateBuffers();

			GL45.glNamedBufferStorage(newHandle, capacity, 0);

			GL45.glCopyNamedBufferSubData(oldHandle, newHandle, 0, 0, this.capacity);

			deleteInternal(oldHandle);

			handle(newHandle);
		} else {
			GL45.glNamedBufferStorage(handle(), capacity, 0);
		}
		this.capacity = capacity;
		FlwMemoryTracker._allocGpuMemory(this.capacity);
	}

	@Override
	protected void deleteInternal(int handle) {
		GlStateManager._glDeleteBuffers(handle);
	}

	@Override
	public void delete() {
		super.delete();
		FlwMemoryTracker._freeGpuMemory(capacity);
	}
}
