package com.jozufozu.flywheel.lib.transform;

import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import com.mojang.blaze3d.vertex.PoseStack;

/**
 * A wrapper around {@link PoseStack} that implements {@link TransformStack}.
 * <br>
 * Only one instance of this class should exist per {@link PoseStack}.
 */
public class PoseTransformStack implements TransformStack<PoseTransformStack> {
	public final PoseStack stack;

	@ApiStatus.Internal
	public PoseTransformStack(PoseStack stack) {
		this.stack = stack;
	}

	@Override
	public PoseTransformStack rotate(Quaternionf quaternion) {
		stack.mulPose(quaternion);
		return this;
	}

	@Override
	public PoseTransformStack rotateAround(Quaternionf quaternion, float x, float y, float z) {
		stack.rotateAround(quaternion, x, y, z);
		return this;
	}

	@Override
	public PoseTransformStack scale(float factorX, float factorY, float factorZ) {
		stack.scale(factorX, factorY, factorZ);
		return this;
	}

	@Override
	public PoseTransformStack mulPose(Matrix4f pose) {
		stack.last()
				.pose()
				.mul(pose);
		return this;
	}

	@Override
	public PoseTransformStack mulNormal(Matrix3f normal) {
		stack.last()
				.normal()
				.mul(normal);
		return this;
	}

	@Override
	public PoseTransformStack pushPose() {
		stack.pushPose();
		return this;
	}

	@Override
	public PoseTransformStack popPose() {
		stack.popPose();
		return this;
	}

	@Override
	public PoseTransformStack translate(double x, double y, double z) {
		stack.translate(x, y, z);
		return this;
	}
}
