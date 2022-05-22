package com.jozufozu.flywheel.core.crumbling;

import com.jozufozu.flywheel.core.shader.WorldProgram;

import net.minecraft.resources.ResourceLocation;

public class CrumblingProgram extends WorldProgram {
	protected int uCrumblingTex;

	public CrumblingProgram(ResourceLocation name, int handle) {
		super(name, handle);
	}

	@Override
	protected void registerSamplers() {
		uCrumblingTex = setSamplerBinding("uCrumblingTex", 0);
	}
}
