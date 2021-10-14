package com.jozufozu.flywheel;

import net.minecraft.resources.ResourceLocation;

public class Flywheel {

	public static final String ID = "flywheel";

	public static ResourceLocation rl(String path) {
		return new ResourceLocation(ID, path);
	}
}
