package dev.engine_room.flywheel.backend.mixin;

import java.nio.ByteBuffer;

import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.engine_room.flywheel.backend.engine.uniform.FogUniforms;
import net.minecraft.client.renderer.fog.FogRenderer;

@Mixin(FogRenderer.class)
abstract class FogRendererMixin {
	@Inject(method = "updateBuffer", at = @At("HEAD"))
	private static void flywheel$updateFogUniform(ByteBuffer buffer, int position, Vector4f fogColor, float environmentalStart, float environmentalEnd, float renderDistanceStart, float renderDistanceEnd, float skyEnd, float cloudEnd, CallbackInfo ci) {
		FogUniforms.update(fogColor, environmentalStart, environmentalEnd, renderDistanceStart, renderDistanceEnd);
	}
}
