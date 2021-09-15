package com.jozufozu.flywheel.core.shader;

import org.lwjgl.opengl.GL20;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.core.shader.extension.IExtensionInstance;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.resources.ResourceLocation;

public class FogMode implements IExtensionInstance {

	public static final ResourceLocation NAME = new ResourceLocation(Flywheel.ID, "fog_linear");

	private final int uFogColor;
	private final int uFogRange;

	public FogMode(GlProgram program) {
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
