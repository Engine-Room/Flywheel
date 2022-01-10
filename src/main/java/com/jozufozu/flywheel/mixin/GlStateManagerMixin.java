package com.jozufozu.flywheel.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.backend.gl.GlStateTracker;
import com.jozufozu.flywheel.backend.gl.buffer.GlBufferType;
import com.mojang.blaze3d.platform.GlStateManager;

@Mixin(value = GlStateManager.class, remap = false)
public class GlStateManagerMixin {

	@Inject(method = "_glBindBuffer", at = @At("TAIL"))
	private static void onBindBuffer(int pTarget, int pBuffer, CallbackInfo ci) {
		GlStateTracker._setBuffer(GlBufferType.fromTarget(pTarget), pBuffer);
	}

	@Inject(method = "_glBindVertexArray", at = @At("TAIL"))
	private static void onBindVertexArray(int pArray, CallbackInfo ci) {
		GlStateTracker._setVertexArray(pArray);
	}

	@Inject(method = "_glUseProgram", at = @At("TAIL"))
	private static void onUseProgram(int pProgram, CallbackInfo ci) {
		GlStateTracker._setProgram(pProgram);
	}
}
