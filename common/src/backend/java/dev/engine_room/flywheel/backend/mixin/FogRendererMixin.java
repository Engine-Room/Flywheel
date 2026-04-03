package dev.engine_room.flywheel.backend.mixin;

import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;

import dev.engine_room.flywheel.backend.engine.uniform.FogUniforms;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;

@Mixin(FogRenderer.class)
abstract class FogRendererMixin {
	@Inject(method = "setupFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/fog/FogRenderer;computeFogColor(Lnet/minecraft/client/Camera;FLnet/minecraft/client/multiplayer/ClientLevel;IFLorg/joml/Vector4f;)V"))
	private static void flywheel$onReturnSetupFog(CallbackInfoReturnable<Vector4f> cir, @Local FogData fogData) {
		FogUniforms.update(fogData.color, fogData.environmentalStart, fogData.environmentalEnd, fogData.renderDistanceStart, fogData.renderDistanceEnd, fogData.skyEnd, fogData.cloudEnd);
	}
}
