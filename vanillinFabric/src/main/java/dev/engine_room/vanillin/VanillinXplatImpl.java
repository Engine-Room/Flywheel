package dev.engine_room.vanillin;

import net.fabricmc.loader.api.FabricLoader;

public class VanillinXplatImpl implements VanillinXplat {
	@Override
	public boolean isDevelopmentEnvironment() {
		return FabricLoader.getInstance()
				.isDevelopmentEnvironment();
	}
}
