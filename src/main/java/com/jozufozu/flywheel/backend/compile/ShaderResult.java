package com.jozufozu.flywheel.backend.compile;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.gl.shader.GlShader;

public sealed interface ShaderResult {
	@Nullable
	default GlShader unwrap() {
		if (this instanceof Success s) {
			return s.shader();
		}
		return null;
	}

	record Success(GlShader shader, String infoLog) implements ShaderResult {
	}

	record Failure(FailedCompilation failure) implements ShaderResult {
	}

	static ShaderResult success(GlShader program, String infoLog) {
		return new Success(program, infoLog);
	}

	static ShaderResult failure(FailedCompilation failure) {
		return new Failure(failure);
	}
}
