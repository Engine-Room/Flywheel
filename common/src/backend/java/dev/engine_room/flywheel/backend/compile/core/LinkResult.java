package dev.engine_room.flywheel.backend.compile.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.backend.gl.shader.GlProgram;

public sealed interface LinkResult {
	@Nullable
	default GlProgram unwrap() {
		return null;
	}

	record Success(GlProgram program, String log) implements LinkResult {
		@Override
		@NotNull
		public GlProgram unwrap() {
			return program;
		}
	}

	record Failure(String failure) implements LinkResult {
	}

	static LinkResult success(GlProgram program, String log) {
		return new Success(program, log);
	}

	static LinkResult failure(String failure) {
		return new Failure(failure);
	}
}
