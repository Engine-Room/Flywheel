package dev.engine_room.flywheel.lib.transform;

import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix3fc;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;

import com.mojang.blaze3d.vertex.PoseStack;

/**
 * A wrapper around {@link PoseStack} that implements {@link TransformStack}.
 * <br>
 * Only one instance of this class should exist per {@link PoseStack}.
 */
public final class PoseTransformStack implements TransformStack<PoseTransformStack> {
	private final PoseStack stack;

	/**
	 * Use {@link TransformStack#of(PoseStack)}.
	 */
	@ApiStatus.Internal
	public PoseTransformStack(PoseStack stack) {
		this.stack = stack;
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
	public PoseTransformStack mulPose(Matrix4fc pose) {
		stack.last()
				.pose()
				.mul(pose);
		return this;
	}

	@Override
	public PoseTransformStack mulNormal(Matrix3fc normal) {
		stack.last()
				.normal()
				.mul(normal);
		return this;
	}

	@Override
	public PoseTransformStack rotateAround(Quaternionfc quaternion, float x, float y, float z) {
		PoseStack.Pose pose = stack.last();
		pose.pose().rotateAround(quaternion, x, y, z);
		pose.normal().rotate(quaternion);
		return this;
	}

	@Override
	public PoseTransformStack translate(float x, float y, float z) {
		stack.translate(x, y, z);
		return this;
	}

	@Override
	public PoseTransformStack rotate(Quaternionfc quaternion) {
		PoseStack.Pose pose = stack.last();
		pose.pose().rotate(quaternion);
		pose.normal().rotate(quaternion);
		return this;
	}

	@Override
	public PoseTransformStack scale(float factorX, float factorY, float factorZ) {
		stack.scale(factorX, factorY, factorZ);
		return this;
	}

	public PoseStack unwrap() {
		return stack;
	}
}
