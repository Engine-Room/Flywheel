package dev.engine_room.flywheel.impl.mixin.fix;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

@Mixin(PoseStack.class)
abstract class FixNormalScalingMixin {
	/**
	 * Minecraft negates the normal matrix if all scales are equal and negative, but
	 * does not return afterward. This allows the rest of the method's logic to be
	 * applied, which negates the matrix again, resulting in the matrix being the
	 * same as in the beginning.
	 */
	@Inject(method = "scale(FFF)V", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;scale(FFF)Lorg/joml/Matrix4f;", shift = Shift.AFTER, remap = false), cancellable = true)
	private void flywheel$returnAfterNegate(float x, float y, float z, CallbackInfo ci) {
		ci.cancel();
	}

	//fixme not needed anymore?
//	/**
//	 * Minecraft takes the inverse cube root of the product of all scales to provide a
//	 * rough estimate for normalization so that it does not need to be done later. It
//	 * does not make sense for this "normalization factor" to be negative though, as
//	 * that would invert all normals. Additionally, Minecraft's fastInvCubeRoot method
//	 * does not work for negative numbers.
//	 */
//	@ModifyArg(method = "scale(FFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;fastInvCubeRoot(F)F"))
//	private float flywheel$absInvCbrtInput(float input) {
//		return Math.abs(input);
//	}
}
