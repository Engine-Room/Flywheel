package dev.engine_room.vanillin;

import dev.engine_room.vanillin.forge.mixin.item.ItemColorsAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.loading.FMLEnvironment;

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
}
