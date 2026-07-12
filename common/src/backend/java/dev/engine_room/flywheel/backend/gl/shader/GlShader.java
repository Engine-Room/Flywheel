package dev.engine_room.flywheel.backend.gl.shader;

import com.mojang.blaze3d.opengl.GlStateManager;

import dev.engine_room.flywheel.backend.gl.GlObject;

public class GlShader extends GlObject {

	public final ShaderType type;
	private final String name;

	public GlShader(int handle, ShaderType type, String name) {
		this.type = type;
		this.name = name;

		handle(handle);
	}

	@Override
	protected void deleteInternal(int handle) {
		GlStateManager.glDeleteShader(handle);
	}

	@Override
	public String toString() {
		return "GlShader{" + type.name + handle() + " " + name + "}";
	}

}
