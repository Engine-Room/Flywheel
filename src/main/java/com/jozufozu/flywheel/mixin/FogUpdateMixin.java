package com.jozufozu.flywheel.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.core.Components;

import net.minecraft.client.renderer.FogRenderer;

@Mixin(FogRenderer.class)
public class FogUpdateMixin {

	@Inject(method = "setupNoFog", at = @At("TAIL"))
	private static void onNoFog(CallbackInfo ci) {
		flywheel$updateFog();
	}

	@Inject(method = "setupFog(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/FogRenderer$FogMode;FZF)V", remap = false, at = @At("TAIL"))
	private static void onFog(CallbackInfo ci) {
		flywheel$updateFog();
	}

	@Inject(method = "levelFogColor", at = @At("TAIL"))
	private static void onFogColor(CallbackInfo ci) {
		flywheel$updateFog();
	}

	private static void flywheel$updateFog() {
		Components.FOG_PROVIDER.update();
	}
}
