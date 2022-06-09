package com.jozufozu.flywheel.backend.gl.buffer;

import static org.lwjgl.opengl.GL30.GL_MAP_WRITE_BIT;
import static org.lwjgl.opengl.GL44.GL_MAP_COHERENT_BIT;
import static org.lwjgl.opengl.GL44.GL_MAP_PERSISTENT_BIT;

import java.nio.ByteBuffer;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL32;
import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.backend.gl.GlFence;
import com.jozufozu.flywheel.backend.gl.error.GlError;
import com.jozufozu.flywheel.backend.gl.error.GlException;
import com.jozufozu.flywheel.backend.gl.versioned.GlCompat;

public class PersistentGlBuffer extends GlBuffer {

	@Nullable
	private MappedBuffer access;
	int flags;
	private final GlFence fence;

	public PersistentGlBuffer(GlBufferType type) {
		super(type);

		flags = GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT;
		fence = new GlFence();
	}

	@Override
	public void doneForThisFrame() {
		fence.post();
	}

	@Override
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
			GlCompat.getInstance().bufferStorage.bufferStorage(type, this.size, flags);
			return true;
		}

		if (size > this.size) {
			var oldSize = this.size;
			this.size = size + growthMargin;

			fence.clear();

			realloc(this.size, oldSize);

			access = null;
			return true;
		}

		return false;
	}

	@Override
	public void upload(ByteBuffer directBuffer) {
		ensureCapacity(directBuffer.capacity());

		var access = getWriteAccess();

		ByteBuffer ourBuffer = access.unwrap();

		ourBuffer.reset();

		MemoryUtil.memCopy(directBuffer, ourBuffer);

		int uploadSize = directBuffer.remaining();
		int ourSize = ourBuffer.capacity();

		if (uploadSize < ourSize) {
			long clearFrom = access.getMemAddress() + uploadSize;
			MemoryUtil.memSet(clearFrom, 0, ourSize - uploadSize);
		}
	}

	private void mapToClientMemory() {
		bind();
		ByteBuffer byteBuffer = GL32.glMapBufferRange(type.glEnum, 0, size, flags);

		if (byteBuffer == null) {
			throw new GlException(GlError.poll(), "Could not map buffer");
		}

		access = new MappedBuffer(this, byteBuffer, 0, size);
	}

	private void realloc(long newSize, long oldSize) {
		int oldHandle = handle();
		int newHandle = GL32.glGenBuffers();

		GlBufferType.COPY_READ_BUFFER.bind(oldHandle);
		type.bind(newHandle);

		GlCompat.getInstance().bufferStorage.bufferStorage(type, newSize, flags);

		GL32.glCopyBufferSubData(GlBufferType.COPY_READ_BUFFER.glEnum, type.glEnum, 0, 0, oldSize);

		delete();
		setHandle(newHandle);
	}

	@Override
	public MappedBuffer map() {
		return getWriteAccess()
				.position(0);
	}

	private MappedBuffer getWriteAccess() {
		if (access == null) {
			mapToClientMemory();
		} else {
			fence.waitSync(); // FIXME: Hangs too much, needs double/triple buffering
		}

		return access;
	}

	@Override
	public boolean isPersistent() {
		return true;
	}
}
