package dev.engine_room.vanillin;

import net.neoforged.fml.loading.FMLEnvironment;

public class VanillinXplatImpl implements VanillinXplat {
	@Override
	public boolean isDevelopmentEnvironment() {
		return !FMLEnvironment.production;
	}
}
