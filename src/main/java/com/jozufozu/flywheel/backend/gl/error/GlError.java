package com.jozufozu.flywheel.backend.gl.error;

import java.util.function.Supplier;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

public enum GlError {
	INVALID_ENUM(GL20.GL_INVALID_ENUM),
	INVALID_VALUE(GL20.GL_INVALID_VALUE),
	INVALID_OPERATION(GL20.GL_INVALID_OPERATION),
	INVALID_FRAMEBUFFER_OPERATION(GL30.GL_INVALID_FRAMEBUFFER_OPERATION),
	OUT_OF_MEMORY(GL20.GL_OUT_OF_MEMORY),
	STACK_UNDERFLOW(GL20.GL_STACK_UNDERFLOW),
	STACK_OVERFLOW(GL20.GL_STACK_OVERFLOW),
	;

	private static final Int2ObjectMap<GlError> errorLookup = new Int2ObjectArrayMap<>();

	static {
		errorLookup.defaultReturnValue(null);
		for (GlError value : values()) {
			errorLookup.put(value.glEnum, value);
		}
	}

	final int glEnum;

	GlError(int glEnum) {
		this.glEnum = glEnum;
	}

	// Great for use in your debugger's expression evaluator
	public static GlError poll() {
		return errorLookup.get(GL20.glGetError());
	}

	public static void pollAndThrow(Supplier<String> context) {
// 		This was a bad idea.
//		GlError err = GlError.poll();
//		if (err != null) {
//			Flywheel.LOGGER.error("{}: {}", err.name(), context.get());
//		}
	}
}
