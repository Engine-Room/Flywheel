package com.jozufozu.flywheel.mixin.atlas;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.core.crumbling.AtlasInfo;

import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

@Mixin(TextureAtlas.class)
public abstract class AtlasDataMixin {

	@Shadow
	public abstract ResourceLocation location();

	@Inject(method = "upload", at = @At("RETURN"))
	public void stealAtlasData(SpriteLoader.Preparations preparations, CallbackInfo ci) {
		AtlasInfo._setAtlasData(location(), preparations);
	}
}
