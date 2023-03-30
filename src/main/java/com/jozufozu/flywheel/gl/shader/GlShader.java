package com.jozufozu.flywheel.gl.shader;

import org.lwjgl.opengl.GL20;

import com.jozufozu.flywheel.gl.GlObject;

public class GlShader extends GlObject {

	public final ShaderType type;
	private final String name;

	public GlShader(int handle, ShaderType type, String name) {
		this.type = type;
		this.name = name;

		setHandle(handle);
	}

	@Override
	protected void deleteInternal(int handle) {
		GL20.glDeleteShader(handle);
	}

	@Override
	public String toString() {
		return "GlShader{" + type.name + handle() + " " + name + "}";
	}

}
