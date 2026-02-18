package dev.engine_room.flywheel.backend.gl.buffer;

import com.mojang.blaze3d.opengl.GlConst;

/**
 * Gives a hint to the driver about how you intend to use a buffer. For a detailed explanation, see
 * <a href="https://www.khronos.org/opengl/wiki/Buffer_Object#Buffer_Object_Usage">this article</a>.
 */
@Deprecated(forRemoval = true)
public enum GlBufferUsage {
	STREAM_DRAW(GlConst.GL_STREAM_DRAW),
	STREAM_READ(GlConst.GL_STREAM_READ),
	STREAM_COPY(GlConst.GL_STREAM_COPY),
	STATIC_DRAW(GlConst.GL_STATIC_DRAW),
	STATIC_READ(GlConst.GL_STATIC_READ),
	STATIC_COPY(GlConst.GL_STATIC_COPY),
	DYNAMIC_DRAW(GlConst.GL_DYNAMIC_DRAW),
	DYNAMIC_READ(GlConst.GL_DYNAMIC_READ),
	DYNAMIC_COPY(GlConst.GL_DYNAMIC_COPY),
	;

	public final int glEnum;

	GlBufferUsage(int glEnum) {
		this.glEnum = glEnum;
	}
}
