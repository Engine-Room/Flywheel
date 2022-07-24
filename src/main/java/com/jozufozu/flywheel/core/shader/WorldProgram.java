package com.jozufozu.flywheel.core.shader;

import com.jozufozu.flywheel.backend.gl.shader.GlProgram;

import net.minecraft.resources.ResourceLocation;

public class WorldProgram extends GlProgram {

	// TODO: sampler registry?
	protected int diffuseTex;
	protected int overlayTex;
	protected int lightTex;

	public WorldProgram(ResourceLocation name, int handle) {
		super(name, handle);

		bind();
		registerSamplers();
		unbind();
	}

	protected void registerSamplers() {
		diffuseTex = setSamplerBinding("flw_diffuseTex", 0);
		overlayTex = setSamplerBinding("flw_overlayTex", 1);
		lightTex = setSamplerBinding("flw_lightTex", 2);
	}
}
