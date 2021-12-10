package com.jozufozu.flywheel.backend.gl.buffer;

import org.lwjgl.opengl.GL30;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.error.GlError;
import com.jozufozu.flywheel.backend.gl.error.GlException;
import com.jozufozu.flywheel.util.StringUtil;
import com.mojang.blaze3d.platform.GlStateManager;

public class MappedBufferRange extends MappedBuffer {

	long offset, length;
	int access;

	public MappedBufferRange(GlBuffer buffer, long offset, long length, int access) {
		super(buffer);
		this.offset = offset;
		this.length = length;
		this.access = access;
	}

	@Override
	public MappedBuffer position(int p) {
		if (p < offset || p >= offset + length) {
			throw new IndexOutOfBoundsException("Index " + p + " is not mapped");
		}
		return super.position(p - (int) offset);
	}

	@Override
	protected void checkAndMap() {
		if (!mapped) {
			setInternal(GL30.glMapBufferRange(owner.type.glEnum, offset, length, access));

			GlError.pollAndThrow(() -> StringUtil.args("mapBufferRange", owner.type, offset, length, access));
			mapped = true;
		}
	}
}
