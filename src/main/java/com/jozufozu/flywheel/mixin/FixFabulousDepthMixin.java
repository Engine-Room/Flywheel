package com.jozufozu.flywheel.mixin;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;

@Mixin(LevelRenderer.class)
public class FixFabulousDepthMixin {

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/client/renderer/PostChain;process(F)V"))
	private void disableTransparencyShaderDepth(PoseStack p_228426_1_, float p_228426_2_, long p_228426_3_, boolean p_228426_5_, Camera p_228426_6_, GameRenderer p_228426_7_, LightTexture p_228426_8_, Matrix4f p_228426_9_, CallbackInfo ci) {
		GlStateManager._depthMask(false);
	}
}
