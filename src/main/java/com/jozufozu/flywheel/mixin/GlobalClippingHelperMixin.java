package com.jozufozu.flywheel.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.core.Clipping;

import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.util.math.vector.Matrix4f;

@Mixin(ClippingHelper.class)
public class GlobalClippingHelperMixin {

	@Inject(at = @At("TAIL"), method = "<init>")
	private void init(Matrix4f p_i226026_1_, Matrix4f p_i226026_2_, CallbackInfo ci) {
		Clipping.HELPER = (ClippingHelper) (Object) this;
	}
}
