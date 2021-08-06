package com.jozufozu.flywheel.mixin;

import com.mojang.blaze3d.systems.RenderSystem;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.core.shader.GlFog;
import com.mojang.blaze3d.platform.GlStateManager;

@Mixin(RenderSystem.class)
public class FogColorTrackerMixin {
	@Inject(at = @At("TAIL"), method = "_setShaderFogColor")
	private static void copyFogColor(float r, float g, float b, float a, CallbackInfo ci) {
		GlFog.FOG_COLOR = new float[]{r, g, b, a};
	}
}
