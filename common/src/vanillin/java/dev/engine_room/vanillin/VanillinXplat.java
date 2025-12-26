package dev.engine_room.vanillin;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.api.internal.DependencyInjection;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.Item;

public interface VanillinXplat {
	VanillinXplat INSTANCE = DependencyInjection.load(VanillinXplat.class, "dev.engine_room.vanillin.VanillinXplatImpl");

	boolean isDevelopmentEnvironment();

	@Nullable ItemColor itemColors(Item item);

	boolean isModLoaded(String modId);
}
