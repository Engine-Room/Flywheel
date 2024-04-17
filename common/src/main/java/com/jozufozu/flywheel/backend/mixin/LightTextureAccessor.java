package com.jozufozu.flywheel.backend.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;

@Mixin(LightTexture.class)
public interface LightTextureAccessor {
	@Accessor("lightTexture")
	DynamicTexture flywheel$texture();
}
