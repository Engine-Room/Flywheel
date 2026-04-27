package dev.engine_room.flywheel.backend.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.engine_room.flywheel.backend.engine.uniform.FogUniforms;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;

@Mixin(FogRenderer.class)
abstract class FogRendererMixin {
	@Inject(method = "updateBuffer(Lnet/minecraft/client/renderer/fog/FogData;)V", at = @At("HEAD"))
	private static void flywheel$onReturnSetupFog(FogData fog, CallbackInfo ci) {
		FogUniforms.update(fog.color, fog.environmentalStart, fog.environmentalEnd, fog.renderDistanceStart, fog.renderDistanceEnd, fog.skyEnd, fog.cloudEnd);
	}
}
