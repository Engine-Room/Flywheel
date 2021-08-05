package com.jozufozu.flywheel.backend.pipeline;

import com.jozufozu.flywheel.backend.gl.shader.ShaderType;

public class Shader {

	private final GLSLVersion version;
	private final CharSequence source;

	public Shader(GLSLVersion version, CharSequence source) {
		this.version = version;
		this.source = source;
	}

	public GlShader create(ShaderType type) {

		StringBuilder source = new StringBuilder();

		source.append("#version ")
				.append(version.version)
				.append('\n');

		source.append("#define ")
				.append(type.define)
				.append('\n');

		source.append(this.source);

		return new GlShader(type, source);
	}
}
