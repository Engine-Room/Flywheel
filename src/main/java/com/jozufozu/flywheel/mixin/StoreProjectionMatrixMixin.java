package com.jozufozu.flywheel.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.backend.Backend;

import net.minecraft.client.renderer.GameRenderer;

@Mixin(GameRenderer.class)
public abstract class StoreProjectionMatrixMixin {

	@Unique
	private boolean shouldCopy = false;

	/**
	 * We only want to copy the projection matrix if it is going to be used to render the world.
	 * We don't care about the mat for your hand.
	 */
	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;resetProjectionMatrix(Lcom/mojang/math/Matrix4f;)V"))
	private void projectionMatrixReady(float p_228378_1_, long p_228378_2_, PoseStack p_228378_4_, CallbackInfo ci) {
		shouldCopy = true;
	}

	@Inject(method = "resetProjectionMatrix", at = @At("TAIL"))
	private void onProjectionMatrixLoad(Matrix4f projection, CallbackInfo ci) {
		if (shouldCopy) {
			Backend.getInstance()
					.setProjectionMatrix(projection.copy());
			shouldCopy = false;
		}
	}
}
