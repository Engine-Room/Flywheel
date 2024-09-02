package com.jozufozu.flywheel.backend.gl.buffer;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL20;

import com.jozufozu.flywheel.backend.gl.GlObject;

public abstract class GlBuffer extends GlObject {

	protected final GlBufferType type;

	/**
	 * The size (in bytes) of the buffer on the GPU.
	 */
	protected long capacity;

	/**
	 * How much extra room to give the buffer when we reallocate.
	 */
	protected int growthMargin;

	public GlBuffer(GlBufferType type) {
		_create();
		this.type = type;
	}

	public void setGrowthMargin(int growthMargin) {
		this.growthMargin = growthMargin;
	}

	public long getCapacity() {
		return capacity;
	}

	public MappedBuffer getBuffer() {
		return getBuffer(0, capacity);
	}

	public abstract MappedBuffer getBuffer(long offset, long length);

	/**
	 * Ensure that the buffer has at least enough room to store size bytes.
	 *
	 * @return true if the buffer grew.
	 */
	public boolean ensureCapacity(long size) {
		if (size > capacity) {
			capacity = size + growthMargin;
			alloc(capacity);
			return true;
		}

		return false;
	}

	/**
	 * Call this after all draw calls using this buffer are complete.
	 */
	public void doneForThisFrame() {

	}

	protected abstract void alloc(long size);

	public abstract void upload(ByteBuffer directBuffer);

	public void bind() {
		type.bind(handle());
	}

	public void unbind() {
		type.unbind();
	}

	protected void _create() {
		setHandle(GL20.glGenBuffers());
	}

	protected void deleteInternal(int handle) {
		GL20.glDeleteBuffers(handle);
	}
}
