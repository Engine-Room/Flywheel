package com.jozufozu.flywheel.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.util.Textures;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.resources.ResourceLocation;

@Mixin(RenderSystem.class)
public class RenderTexturesMixin {

	@Inject(method = "_setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/AbstractTexture;getId()I"))
	private static void storeTextureLoc(int pShaderTexture, ResourceLocation pTextureId, CallbackInfo ci) {
		Textures._setShaderTexture(pShaderTexture, pTextureId);
	}
}
