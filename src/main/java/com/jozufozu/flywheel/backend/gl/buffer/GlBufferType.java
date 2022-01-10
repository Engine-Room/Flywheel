package com.jozufozu.flywheel.backend.gl.buffer;

import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.GL43;

import com.jozufozu.flywheel.backend.gl.GlStateTracker;
import com.mojang.blaze3d.platform.GlStateManager;

public enum GlBufferType {
	ARRAY_BUFFER(GL15C.GL_ARRAY_BUFFER),
	ELEMENT_ARRAY_BUFFER(GL15C.GL_ELEMENT_ARRAY_BUFFER),
	PIXEL_PACK_BUFFER(GL21.GL_PIXEL_PACK_BUFFER),
	PIXEL_UNPACK_BUFFER(GL21.GL_PIXEL_UNPACK_BUFFER),
	TRANSFORM_FEEDBACK_BUFFER(GL30.GL_TRANSFORM_FEEDBACK_BUFFER),
	UNIFORM_BUFFER(GL31.GL_UNIFORM_BUFFER),
	TEXTURE_BUFFER(GL31.GL_TEXTURE_BUFFER),
	COPY_READ_BUFFER(GL31.GL_COPY_READ_BUFFER),
	COPY_WRITE_BUFFER(GL31.GL_COPY_WRITE_BUFFER),
	DRAW_INDIRECT_BUFFER(GL40.GL_DRAW_INDIRECT_BUFFER),
	ATOMIC_COUNTER_BUFFER(GL42.GL_ATOMIC_COUNTER_BUFFER),
	DISPATCH_INDIRECT_BUFFER(GL43.GL_DISPATCH_INDIRECT_BUFFER),
	SHADER_STORAGE_BUFFER(GL43.GL_SHADER_STORAGE_BUFFER),
	;

	public final int glEnum;

	GlBufferType(int glEnum) {
		this.glEnum = glEnum;
	}

	public static GlBufferType fromTarget(int pTarget) {
		return switch (pTarget) {
			case GL15C.GL_ARRAY_BUFFER -> ARRAY_BUFFER;
			case GL15C.GL_ELEMENT_ARRAY_BUFFER -> ELEMENT_ARRAY_BUFFER;
			case GL21.GL_PIXEL_PACK_BUFFER -> PIXEL_PACK_BUFFER;
			case GL21.GL_PIXEL_UNPACK_BUFFER -> PIXEL_UNPACK_BUFFER;
			case GL30.GL_TRANSFORM_FEEDBACK_BUFFER -> TRANSFORM_FEEDBACK_BUFFER;
			case GL31.GL_UNIFORM_BUFFER -> UNIFORM_BUFFER;
			case GL31.GL_TEXTURE_BUFFER -> TEXTURE_BUFFER;
			case GL31.GL_COPY_READ_BUFFER -> COPY_READ_BUFFER;
			case GL31.GL_COPY_WRITE_BUFFER -> COPY_WRITE_BUFFER;
			case GL40.GL_DRAW_INDIRECT_BUFFER -> DRAW_INDIRECT_BUFFER;
			case GL42.GL_ATOMIC_COUNTER_BUFFER -> ATOMIC_COUNTER_BUFFER;
			case GL43.GL_DISPATCH_INDIRECT_BUFFER -> DISPATCH_INDIRECT_BUFFER;
			case GL43.GL_SHADER_STORAGE_BUFFER -> SHADER_STORAGE_BUFFER;
			default -> throw new IllegalArgumentException("Unknown target: " + pTarget);
		};
	}

    public void bind(int buffer) {
		GlStateManager._glBindBuffer(glEnum, buffer);
	}

	public void unbind() {
		GlStateManager._glBindBuffer(glEnum, 0);
	}

	public int getBoundBuffer() {
		return GlStateTracker.getBuffer(this);
	}
}
