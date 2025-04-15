package dev.engine_room.vanillin;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.vanillin.fabric.mixin.item.ItemColorsAccessor;
import dev.engine_room.vanillin.fabric.mixin.item.MinecraftAccessor;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

public class VanillinXplatImpl implements VanillinXplat {
	@Override
	public boolean isDevelopmentEnvironment() {
		return FabricLoader.getInstance()
				.isDevelopmentEnvironment();
	}

	@Override
	@Nullable
	public ItemColor itemColors(Item item) {
		var itemColors = ((ItemColorsAccessor) ((MinecraftAccessor) Minecraft.getInstance()).vanillin$itemColors()).vanillin$itemColors();
		return itemColors.byId(BuiltInRegistries.ITEM.getId(item));
	}

	@Override
	public boolean isModLoaded(String modId) {
		return FabricLoader.getInstance()
				.isModLoaded(modId);
	}
}
