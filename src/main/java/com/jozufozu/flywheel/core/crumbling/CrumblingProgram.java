package com.jozufozu.flywheel.core.crumbling;

import com.jozufozu.flywheel.core.WorldProgram;

public class CrumblingProgram extends WorldProgram {
	public CrumblingProgram(int handle) {
		super(handle);
	}

	@Override
	protected void registerSamplers() {
		diffuseTex = setSamplerBinding("flw_diffuseTex", 0);
	}
}
