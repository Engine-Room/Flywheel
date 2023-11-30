package com.jozufozu.flywheel.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.lib.uniform.FlwShaderUniforms;

import net.minecraft.client.renderer.FogRenderer;

@Mixin(FogRenderer.class)
abstract class FogUpdateMixin {
	@Unique
	private static void flywheel$updateFog() {
		FlwShaderUniforms.FOG_UPDATE = true;
	}

	@Inject(method = "setupNoFog()V", at = @At("RETURN"))
	private static void flywheel$onNoFog(CallbackInfo ci) {
		flywheel$updateFog();
	}

	@Inject(method = "setupFog(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/FogRenderer$FogMode;FZF)V", at = @At("RETURN"), remap = false)
	private static void flywheel$onFog(CallbackInfo ci) {
		flywheel$updateFog();
	}

	@Inject(method = "levelFogColor()V", at = @At("RETURN"))
	private static void flywheel$onFogColor(CallbackInfo ci) {
		flywheel$updateFog();
	}
}
