package dev.engine_room.vanillin.fabric.mixin.item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.core.IdMapper;

@Mixin(ItemColors.class)
public interface ItemColorsAccessor {
	@Accessor("itemColors")
	IdMapper<ItemColor> vanillin$itemColors();
}
