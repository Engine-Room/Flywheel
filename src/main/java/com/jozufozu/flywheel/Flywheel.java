package com.jozufozu.flywheel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.resources.ResourceLocation;

public class Flywheel {

	public static final String ID = "flywheel";
	public static final Logger LOGGER = LogManager.getLogger(Flywheel.class);

	public static ResourceLocation rl(String path) {
		return new ResourceLocation(ID, path);
	}
}
