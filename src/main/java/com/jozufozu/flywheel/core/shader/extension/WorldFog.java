package com.jozufozu.flywheel.core.shader.extension;

import org.lwjgl.opengl.GL20;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.resources.ResourceLocation;

public class WorldFog implements ExtensionInstance {

	public static final ResourceLocation NAME = Flywheel.rl("fog");

	private final int uFogColor;
	private final int uFogRange;

	public WorldFog(GlProgram program) {
		this.uFogColor = program.getUniformLocation("uFogColor");
		this.uFogRange = program.getUniformLocation("uFogRange");
	}

	@Override
	public void bind() {
		GL20.glUniform2f(uFogRange, RenderSystem.getShaderFogStart(), RenderSystem.getShaderFogEnd());
		GL20.glUniform4fv(uFogColor, RenderSystem.getShaderFogColor());
	}

	@Override
	public ResourceLocation name() {
		return NAME;
	}
}
