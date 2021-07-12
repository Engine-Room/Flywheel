package com.jozufozu.flywheel.mixin.atlas;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.texture.AtlasTexture;

@Mixin(AtlasTexture.SheetData.class)
public interface SheetDataAccessor {

	@Accessor("width")
	int getWidth();

	@Accessor("height")
	int getHeight();

}
