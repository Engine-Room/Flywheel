package dev.engine_room.flywheel.backend.compile.core;

import dev.engine_room.flywheel.backend.gl.shader.GlShader;

public sealed interface ShaderResult {
	GlShader unwrap();

	record Success(GlShader shader, String infoLog) implements ShaderResult {
		@Override
		public GlShader unwrap() {
			return shader;
		}
	}

	record Failure(FailedCompilation failure) implements ShaderResult {
		@Override
		public GlShader unwrap() {
			throw new ShaderException.Compile(failure.generateMessage());
		}
	}

	static ShaderResult success(GlShader program, String infoLog) {
		return new Success(program, infoLog);
	}

	static ShaderResult failure(FailedCompilation failure) {
		return new Failure(failure);
	}
}
