package com.jozufozu.flywheel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resources.ResourceLocation;

public final class Flywheel {
	public static final String ID = "flywheel";
	public static final Logger LOGGER = LoggerFactory.getLogger(ID);

	private Flywheel() {
	}

	public static ResourceLocation rl(String path) {
		return new ResourceLocation(ID, path);
	}
}
