package dev.engine_room.flywheel.backend.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.GpuDevice;

import dev.engine_room.flywheel.backend.engine.uniform.FogUniforms;
import dev.engine_room.flywheel.backend.gl.GlCompat;

@Mixin(value = RenderSystem.class, remap = false)
abstract class RenderSystemMixin {
	@Inject(method = "initRenderer(Lcom/mojang/blaze3d/systems/GpuDevice;)V", at = @At("RETURN"))
	private static void flywheel$onInitRenderer(GpuDevice device, CallbackInfo ci) {
		GlCompat.init();
	}

	@Inject(method = "setShaderFog(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V", at = @At("RETURN"))
	private static void flywheel$onSetShaderFog(GpuBufferSlice fog, CallbackInfo ci) {
		FogUniforms.update();
	}
}
