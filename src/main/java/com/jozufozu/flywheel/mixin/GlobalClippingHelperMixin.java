package com.jozufozu.flywheel.mixin;

import com.mojang.math.Matrix4f;

import net.minecraft.client.renderer.culling.Frustum;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.core.Clipping;

@Mixin(Frustum.class)
public class GlobalFrustumMixin {

	@Inject(at = @At("TAIL"), method = "<init>")
	private void init(Matrix4f p_i226026_1_, Matrix4f p_i226026_2_, CallbackInfo ci) {
		Clipping.HELPER = (Frustum) (Object) this;
	}
}
