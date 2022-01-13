package com.jozufozu.flywheel.core.shader;

import org.lwjgl.opengl.GL20;

import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.mojang.blaze3d.systems.RenderSystem;

public class WorldFog {

	private final int uFogColor;
	private final int uFogRange;

	public WorldFog(GlProgram program) {
		this.uFogColor = program.getUniformLocation("uFogColor");
		this.uFogRange = program.getUniformLocation("uFogRange");
	}

	public void bind() {
		GL20.glUniform2f(uFogRange, RenderSystem.getShaderFogStart(), RenderSystem.getShaderFogEnd());
		GL20.glUniform4fv(uFogColor, RenderSystem.getShaderFogColor());
	}
}
