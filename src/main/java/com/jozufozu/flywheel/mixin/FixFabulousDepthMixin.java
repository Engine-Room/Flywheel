package com.jozufozu.flywheel.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;

@Mixin(LevelRenderer.class)
public class FixFabulousDepthMixin {
	@Inject(method = "renderLevel", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/client/renderer/PostChain;process(F)V"))
	private void flywheel$disableTransparencyShaderDepth(PoseStack poseStack, float partialTick, long finishNanoTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci) {
		GlStateManager._depthMask(false);
	}
}
