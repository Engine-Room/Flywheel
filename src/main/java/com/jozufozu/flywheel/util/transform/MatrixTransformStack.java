package com.jozufozu.flywheel.util.transform;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.util.math.vector.Quaternion;

public class MatrixTransformStack implements TransformStack {

	private final MatrixStack internal;

	public MatrixTransformStack() {
		this(new MatrixStack());
	}

	public MatrixTransformStack(MatrixStack internal) {
		this.internal = internal;
	}

	public static MatrixTransformStack of(MatrixStack ms) {
		return new MatrixTransformStack(ms);
	}

	public MatrixStack unwrap() {
		return internal;
	}

	public MatrixTransformStack setIdentity() {
		if (internal.clear()) {
			MatrixStack.Entry last = internal.last();

			last.normal()
					.setIdentity();
			last.pose()
					.setIdentity();
		} else {
			internal.popPose();
			internal.pushPose();
		}

		return this;
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
