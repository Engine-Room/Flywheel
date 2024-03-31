package com.jozufozu.flywheel.backend.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.backend.engine.uniform.Uniforms;
import com.jozufozu.flywheel.backend.gl.GlCompat;
import com.mojang.blaze3d.systems.RenderSystem;

@Mixin(RenderSystem.class)
abstract class RenderSystemMixin {
	@Inject(method = "initRenderer(IZ)V", at = @At("RETURN"))
	private static void flywheel$onInitRenderer(CallbackInfo ci) {
		GlCompat.init();
	}

	@Inject(method = "setShaderFogStart(F)V", at = @At("RETURN"))
	private static void flywheel$onSetFogStart(CallbackInfo ci) {
		Uniforms.onFogUpdate();
	}

	@Inject(method = "setShaderFogEnd(F)V", at = @At("RETURN"))
	private static void flywheel$onSetFogEnd(CallbackInfo ci) {
		Uniforms.onFogUpdate();
	}

	@Inject(method = "setShaderFogShape(Lcom/mojang/blaze3d/shaders/FogShape;)V", at = @At("RETURN"))
	private static void flywheel$onSetFogShape(CallbackInfo ci) {
		Uniforms.onFogUpdate();
	}

	@Inject(method = "setShaderFogColor(FFFF)V", at = @At("RETURN"))
	private static void flywheel$onSetFogColor(CallbackInfo ci) {
		Uniforms.onFogUpdate();
	}
}
