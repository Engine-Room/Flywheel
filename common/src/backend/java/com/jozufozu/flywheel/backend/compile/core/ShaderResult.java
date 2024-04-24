package com.jozufozu.flywheel.backend.compile.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.backend.gl.shader.GlShader;

public sealed interface ShaderResult {
	@Nullable
	default GlShader unwrap() {
		return null;
	}

	record Success(GlShader shader, String infoLog) implements ShaderResult {
		@Override
		@NotNull
		public GlShader unwrap() {
			return shader;
		}
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
