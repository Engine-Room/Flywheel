package com.jozufozu.flywheel.backend.instancing.compile;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.gl.shader.GlShader;

public sealed interface CompilationResult {
	@Nullable
	default GlShader unwrap() {
		if (this instanceof Success s) {
			return s.shader();
		}
		return null;
	}

	record Success(GlShader shader) implements CompilationResult {
	}

	record Failure(FailedCompilation failure) implements CompilationResult {
	}

	static CompilationResult success(GlShader program) {
		return new Success(program);
	}

	static CompilationResult failure(FailedCompilation failure) {
		return new Failure(failure);
	}
}
