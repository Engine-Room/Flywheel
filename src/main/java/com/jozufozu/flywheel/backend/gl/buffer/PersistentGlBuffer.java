package com.jozufozu.flywheel.backend.gl.buffer;

import static org.lwjgl.opengl.GL30.GL_MAP_WRITE_BIT;
import static org.lwjgl.opengl.GL44.GL_MAP_COHERENT_BIT;
import static org.lwjgl.opengl.GL44.GL_MAP_PERSISTENT_BIT;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL30;

import com.jozufozu.flywheel.backend.gl.GlFence;
import com.jozufozu.flywheel.backend.gl.error.GlError;
import com.jozufozu.flywheel.backend.gl.error.GlException;
import com.jozufozu.flywheel.backend.gl.versioned.GlCompat;

public class PersistentGlBuffer extends GlBuffer implements Mappable {

	private MappedBuffer buffer;
	int flags;

	long size;
	GlFence fence;

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
	protected void alloc(long size) {
		this.size = size;

		if (buffer != null) {
			deleteInternal(handle());
			_create();

			bind();
		}

		fence.clear();

        GlCompat.getInstance().bufferStorage.bufferStorage(type, size, flags);

		ByteBuffer byteBuffer = GL30.glMapBufferRange(type.glEnum, 0, size, flags);

		if (byteBuffer == null) {
			throw new GlException(GlError.poll(), "Could not map buffer");
		}

		buffer = new MappedBuffer(this, byteBuffer, 0, size);
	}

	@Override
	public void upload(ByteBuffer directBuffer) {
		throw new UnsupportedOperationException("FIXME: Nothing calls #upload on a persistent buffer as of 12/10/2021.");
	}

	@Override
	public MappedBuffer getBuffer(long offset, long length) {

		fence.waitSync();

		buffer.position((int) offset);

		return buffer;
	}

	@Override
	public GlBufferType getType() {
		return type;
	}

	@Override
	public boolean isPersistent() {
		return true;
	}
}
