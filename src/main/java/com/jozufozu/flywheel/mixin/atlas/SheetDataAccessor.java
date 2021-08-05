package com.jozufozu.flywheel.mixin.atlas;

import net.minecraft.client.renderer.texture.TextureAtlas;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TextureAtlas.Preparations.class)
public interface SheetDataAccessor {

	@Accessor("width")
	int getWidth();

	@Accessor("height")
	int getHeight();

}
