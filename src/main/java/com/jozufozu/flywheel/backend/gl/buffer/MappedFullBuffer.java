package com.jozufozu.flywheel.backend.gl.buffer;

import org.lwjgl.opengl.GL15;

import com.jozufozu.flywheel.backend.gl.error.GlError;
import com.jozufozu.flywheel.backend.gl.error.GlException;
import com.jozufozu.flywheel.util.StringUtil;

public class MappedFullBuffer extends MappedBuffer {

	MappedBufferUsage usage;

	public MappedFullBuffer(GlBuffer buffer, MappedBufferUsage usage) {
		super(buffer);
		this.usage = usage;
	}

	@Override
	protected void checkAndMap() {
		if (!mapped) {
			setInternal(GL15.glMapBuffer(owner.type.glEnum, usage.glEnum));

			GlError error = GlError.poll();

			if (error != null) {
				throw new GlException(error, StringUtil.args("mapBuffer", owner.type, usage));
			}

			mapped = true;
		}
	}
}
