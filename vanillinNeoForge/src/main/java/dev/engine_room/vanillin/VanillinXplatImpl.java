package dev.engine_room.vanillin;

import net.minecraft.world.item.Item;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.LoadingModList;

public class VanillinXplatImpl implements VanillinXplat {
	@Override
	public boolean isDevelopmentEnvironment() {
		return !FMLEnvironment.isProduction();
	}

	@Override
	public Object itemColors(Item item) {
		return null;
	}

	@Override
	public boolean isModLoaded(String modId) {
		return LoadingModList.get()
				.getModFileById(modId) != null;
	}
}
