package com.jozufozu.flywheel.core.shader;

import org.lwjgl.opengl.GL20;

import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.mojang.blaze3d.systems.RenderSystem;

public class WorldFog {

	private final int uFogRange;
	private final int uFogColor;
	private final int uFogShape;

	public WorldFog(GlProgram program) {
		this.uFogRange = program.getUniformLocation("uFogRange");
		this.uFogColor = program.getUniformLocation("uFogColor");
		this.uFogShape = program.getUniformLocation("uFogShape");
	}

	public void uploadUniforms() {
		GL20.glUniform2f(uFogRange, RenderSystem.getShaderFogStart(), RenderSystem.getShaderFogEnd());
		GL20.glUniform4fv(uFogColor, RenderSystem.getShaderFogColor());
		GL20.glUniform1i(uFogShape, RenderSystem.getShaderFogShape().getIndex());
	}
}
