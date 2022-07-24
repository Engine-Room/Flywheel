package com.jozufozu.flywheel.core.crumbling;

import com.jozufozu.flywheel.core.WorldProgram;

import net.minecraft.resources.ResourceLocation;

public class CrumblingProgram extends WorldProgram {
	public CrumblingProgram(ResourceLocation name, int handle) {
		super(name, handle);
	}

	@Override
	protected void registerSamplers() {
		diffuseTex = setSamplerBinding("flw_diffuseTex", 0);
	}
}
