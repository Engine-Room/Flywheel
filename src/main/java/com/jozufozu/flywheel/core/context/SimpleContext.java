package com.jozufozu.flywheel.core.context;

import com.jozufozu.flywheel.api.context.Context;
import com.jozufozu.flywheel.gl.shader.GlProgram;

import net.minecraft.resources.ResourceLocation;

public record SimpleContext(ResourceLocation vertexShader, ResourceLocation fragmentShader) implements Context {
	@Override
	public void onProgramLink(GlProgram program) {
		program.bind();
		program.setSamplerBinding("flw_diffuseTex", 0);
		program.setSamplerBinding("flw_overlayTex", 1);
		program.setSamplerBinding("flw_lightTex", 2);
		GlProgram.unbind();
	}

	@Override
	public ResourceLocation vertexShader() {
		return vertexShader;
	}

	@Override
	public ResourceLocation fragmentShader() {
		return fragmentShader;
	}
}
