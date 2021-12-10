package com.jozufozu.flywheel.backend.gl.buffer;

import static org.lwjgl.opengl.GL30.GL_MAP_WRITE_BIT;
import static org.lwjgl.opengl.GL44.GL_MAP_COHERENT_BIT;
import static org.lwjgl.opengl.GL44.GL_MAP_PERSISTENT_BIT;

import java.nio.ByteBuffer;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.GlFence;
import com.jozufozu.flywheel.backend.gl.error.GlError;
import com.jozufozu.flywheel.backend.gl.error.GlException;
import com.jozufozu.flywheel.util.StringUtil;


public class PersistentGlBuffer extends GlBuffer {

	private PersistentMappedBuffer buffer;
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
	public void alloc(long size) {
		this.size = size;

		if (buffer != null) {
			deleteInternal(handle());
			_create();

			bind();
		}

		fence.clear();

		Backend.getInstance().compat.bufferStorage.bufferStorage(type, size, flags);

		GlError.pollAndThrow(() -> StringUtil.args("bufferStorage", type, size, flags));

		buffer = new PersistentMappedBuffer(this);
	}

	@Override
	public void upload(ByteBuffer directBuffer) {

	}

	@Override
	public MappedBuffer getBuffer(int offset, int length) {

		fence.waitSync();

		buffer.position(offset);

		return buffer;
	}
}
