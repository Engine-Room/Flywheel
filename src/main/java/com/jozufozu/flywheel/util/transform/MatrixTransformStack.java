package com.jozufozu.flywheel.util.transform;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;

public class MatrixTransformStack implements TransformStack {

	private final PoseStack internal;

	public MatrixTransformStack() {
		this(new PoseStack());
	}

	public MatrixTransformStack(PoseStack internal) {
		this.internal = internal;
	}

	public static MatrixTransformStack of(PoseStack ms) {
		return new MatrixTransformStack(ms);
	}

	public PoseStack unwrap() {
		return internal;
	}

	@Override
	public TransformStack translate(double x, double y, double z) {
		internal.translate(x, y, z);
		return this;
	}

	@Override
	public TransformStack multiply(Quaternion quaternion) {
		internal.mulPose(quaternion);
		return this;
	}

	@Override
	public TransformStack scale(float factor) {
		internal.scale(factor, factor, factor);
		return this;
	}

	@Override
	public TransformStack push() {
		internal.pushPose();
		return this;
	}

	@Override
	public TransformStack pop() {
		internal.popPose();
		return this;
	}
}
