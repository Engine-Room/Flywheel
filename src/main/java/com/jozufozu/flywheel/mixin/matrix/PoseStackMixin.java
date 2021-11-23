package com.jozufozu.flywheel.mixin.matrix;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;

@Mixin(PoseStack.class)
public abstract class PoseStackMixin implements TransformStack {
	@Shadow
	public abstract void mulPose(Quaternion pQuaternion);

	@Shadow
	public abstract void shadow$scale(float factorX, float factorY, float factorZ);

	@Shadow
	public abstract void shadow$pushPose();

	@Shadow
	public abstract void shadow$popPose();

	@Shadow
	public abstract void shadow$translate(double x, double y, double z);

	@Override
	public TransformStack multiply(Quaternion quaternion) {
		mulPose(quaternion);
		return this;
	}

	@Override
	public TransformStack scale(float factorX, float factorY, float factorZ) {
		shadow$scale(factorX, factorY, factorZ);
		return this;
	}

	@Override
	public TransformStack pushPose() {
		shadow$pushPose();
		return this;
	}

	@Override
	public TransformStack popPose() {
		shadow$popPose();
		return this;
	}

	@Override
	public TransformStack translate(double x, double y, double z) {
		shadow$translate(x, y, z);
		return this;
	}
}
