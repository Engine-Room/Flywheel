package dev.engine_room.vanillin;

import dev.engine_room.vanillin.neoforge.mixin.item.ItemColorsAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.Item;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.LoadingModList;

public class VanillinXplatImpl implements VanillinXplat {
	@Override
	public boolean isDevelopmentEnvironment() {
		return !FMLEnvironment.production;
	}

	@Override
	public ItemColor itemColors(Item item) {
		return ((ItemColorsAccessor) Minecraft.getInstance()
				.getItemColors()).vanillin$itemColors()
				.get(item);
	}

	@Override
	public boolean isModLoaded(String modId) {
		return LoadingModList.get()
				.getModFileById(modId) != null;
	}
}
