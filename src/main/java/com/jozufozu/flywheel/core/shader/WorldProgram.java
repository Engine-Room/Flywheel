package com.jozufozu.flywheel.core.shader;

import com.jozufozu.flywheel.backend.gl.shader.GlProgram;

import net.minecraft.resources.ResourceLocation;

public class WorldProgram extends GlProgram {

	// TODO: sampler registry?
	protected int uBlockAtlas;
	protected int uLightMap;

	public WorldProgram(ResourceLocation name, int handle) {
		super(name, handle);

		bind();
		registerSamplers();
		unbind();
	}

	protected void registerSamplers() {
		uBlockAtlas = setSamplerBinding("uBlockAtlas", 0);
		uLightMap = setSamplerBinding("uLightMap", 2);
	}
}
