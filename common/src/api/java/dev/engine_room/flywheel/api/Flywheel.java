package dev.engine_room.flywheel.api;

import net.minecraft.resources.ResourceLocation;

public final class Flywheel {
	public static final String ID = "flywheel";

	private Flywheel() {
	}

	public static ResourceLocation rl(String path) {
		return new ResourceLocation(ID, path);
	}
}
