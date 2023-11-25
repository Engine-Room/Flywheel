package com.jozufozu.flywheel.mixin;

import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;

import com.jozufozu.flywheel.lib.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;

@Mixin(PoseStack.class)
public abstract class PoseStackMixin implements TransformStack {
	@Override
	public TransformStack pushPose() {
		((PoseStack) (Object) this).pushPose();
		return this;
	}

	@Override
	public TransformStack popPose() {
		((PoseStack) (Object) this).popPose();
		return this;
	}

	@Override
	public TransformStack scale(float factorX, float factorY, float factorZ) {
		((PoseStack) (Object) this).scale(factorX, factorY, factorZ);
		return this;
	}

	@Override
	public TransformStack rotate(Quaternionf quaternion) {
		((PoseStack) (Object) this).mulPose(quaternion);
		return this;
	}

	@Override
	public TransformStack translate(double x, double y, double z) {
		((PoseStack) (Object) this).translate(x, y, z);
		return this;
	}
}
