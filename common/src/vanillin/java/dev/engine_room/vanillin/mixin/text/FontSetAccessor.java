package dev.engine_room.vanillin.mixin.text;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.gui.font.FontSet;

@Mixin(FontSet.class)
public interface FontSetAccessor {
	@Accessor("glyphsByWidth")
	Int2ObjectMap<IntList> vanillin$glyphsByWidth();
}
