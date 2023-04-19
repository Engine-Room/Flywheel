package com.jozufozu.flywheel.backend.compile;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.gl.shader.GlProgram;

public sealed interface LinkResult {

	@Nullable
	default GlProgram unwrap() {
		if (this instanceof Success s) {
			return s.program();
		}
		return null;
	}

	record Success(GlProgram program, String log) implements LinkResult {
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
