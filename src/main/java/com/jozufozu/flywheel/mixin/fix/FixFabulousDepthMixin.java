package com.jozufozu.flywheel.mixin.fix;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.renderer.LevelRenderer;

@Mixin(LevelRenderer.class)
public class FixFabulousDepthMixin {
	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/PostChain;process(F)V", ordinal = 1))
	private void flywheel$disableTransparencyShaderDepth(CallbackInfo ci) {
		GlStateManager._depthMask(false);
	}
}
