package com.jozufozu.flywheel.backend.gl.buffer;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL20;

import com.jozufozu.flywheel.backend.gl.GlObject;
import com.jozufozu.flywheel.backend.gl.versioned.GlCompat;

public abstract class GlBuffer extends GlObject {

	/**
	 * Request a Persistent mapped buffer.
	 *
	 * <p>
	 *     If Persistent buffers are supported, this will provide one. Otherwise it will fall back to a classic mapped
	 *     buffer.
	 * </p>
	 *
	 * @param type The type of buffer you want.
	 * @return A buffer that will be persistent if the driver supports it.
	 */
	public static GlBuffer requestPersistent(GlBufferType type) {
		if (GlCompat.getInstance()
                .bufferStorageSupported()) {
			return new PersistentGlBuffer(type);
		} else {
			return new MappedGlBuffer(type);
		}
	}

	public final GlBufferType type;

	/**
	 * The size (in bytes) of the buffer on the GPU.
	 */
	protected long size;

	/**
	 * How much extra room to give the buffer when we reallocate.
	 */
	protected int growthMargin;

	public GlBuffer(GlBufferType type) {
		setHandle(GL20.glGenBuffers());
		this.type = type;
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

	public abstract void upload(ByteBuffer directBuffer);

	public abstract MappedBuffer map();

	/**
	 * Ensure that the buffer has at least enough room to store {@code size} bytes.
	 *
	 * @return {@code true} if the buffer moved.
	 */
	public abstract boolean ensureCapacity(long size);

	protected void deleteInternal(int handle) {
		GL20.glDeleteBuffers(handle);
	}

	/**
	 * Indicates that this buffer need not be #flush()'d for its contents to sync.
	 * @return true if this buffer is persistently mapped.
	 */
	public abstract boolean isPersistent();
}
