package dev.engine_room.vanillin;

import net.minecraftforge.fml.loading.FMLEnvironment;

public class VanillinXplatImpl implements VanillinXplat {
	@Override
	public boolean isDevelopmentEnvironment() {
		return !FMLEnvironment.production;
	}
}
