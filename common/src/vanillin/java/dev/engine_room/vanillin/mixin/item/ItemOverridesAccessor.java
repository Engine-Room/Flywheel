package dev.engine_room.vanillin.mixin.item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.resources.ResourceLocation;

@Mixin(ItemOverrides.class)
public interface ItemOverridesAccessor {
	@Accessor("properties")
	ResourceLocation[] vanillin$properties();
}
