package dev.engine_room.vanillin;

import net.neoforged.fml.loading.FMLLoader;

public class VanillinXplatImpl implements VanillinXplat {
	@Override
	public boolean isDevelopmentEnvironment() {
		return !FMLLoader.getCurrent().isProduction();
	}

	@Override
	public boolean isModLoaded(String modId) {
		return FMLLoader.getCurrent().getLoadingModList().getModFileById(modId) != null;
	}
}
