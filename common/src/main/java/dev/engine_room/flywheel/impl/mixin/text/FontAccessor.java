package dev.engine_room.flywheel.impl.mixin.text;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.resources.ResourceLocation;

@Mixin(Font.class)
public interface FontAccessor {
	@Invoker("getFontSet")
	FontSet flywheel$getFontSet(ResourceLocation fontLocation);
}
