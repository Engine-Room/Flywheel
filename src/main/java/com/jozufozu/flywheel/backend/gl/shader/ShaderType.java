package com.jozufozu.flywheel.backend.gl.shader;

import org.lwjgl.opengl.GL20;

public enum ShaderType {
	VERTEX("vertex", "VERTEX_SHADER", GL20.GL_VERTEX_SHADER),
	FRAGMENT("fragment", "FRAGMENT_SHADER", GL20.GL_FRAGMENT_SHADER),
	;

	public final String name;
	public final String define;
	public final int glEnum;

	ShaderType(String name, String define, int glEnum) {
		this.name = name;
		this.define = define;
		this.glEnum = glEnum;
	}

	public String getDefineStatement() {
		return "#define " + define + "\n";
	}
}
