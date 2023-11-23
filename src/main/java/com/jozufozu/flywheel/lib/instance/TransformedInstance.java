package com.jozufozu.flywheel.lib.instance;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import com.jozufozu.flywheel.api.instance.InstanceHandle;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.lib.transform.Transform;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.util.Mth;

public class TransformedInstance extends ColoredLitInstance implements Transform<TransformedInstance> {
	private static final Matrix4f ZERO_MATRIX_4f = new Matrix4f();
	private static final Matrix3f ZERO_MATRIX_3f = new Matrix3f();

	public final Matrix4f model = new Matrix4f();
	public final Matrix3f normal = new Matrix3f();

	public TransformedInstance(InstanceType<? extends TransformedInstance> type, InstanceHandle handle) {
		super(type, handle);
	}

	public TransformedInstance setTransform(PoseStack stack) {
		setChanged();

		this.model.set(stack.last()
				.pose());
		this.normal.set(stack.last()
				.normal());
		return this;
	}

	/**
	 * Sets the transform matrices to be all zeros.
	 *
	 * <p>
	 *     This will allow the gpu to quickly discard all geometry for this instance, effectively "turning it off".
	 * </p>
	 */
	public TransformedInstance setEmptyTransform() {
		setChanged();

		model.set(ZERO_MATRIX_4f);
		normal.set(ZERO_MATRIX_3f);
		return this;
	}

	public TransformedInstance loadIdentity() {
		setChanged();

		model.identity();
		normal.identity();
		return this;
	}

	@Override
	public TransformedInstance multiply(Quaternionf quaternion) {
		setChanged();

		model.rotate(quaternion);
		normal.rotate(quaternion);
		return this;
	}

	@Override
	public TransformedInstance scale(float x, float y, float z) {
		setChanged();

		model.scale(x, y, z);

		if (x == y && y == z) {
			if (x < 0.0f) {
				normal.scale(-1.0f);
			}

			return this;
		}

		float invX = 1.0f / x;
		float invY = 1.0f / y;
		float invZ = 1.0f / z;
		float f = Mth.fastInvCubeRoot(Math.abs(invX * invY * invZ));
		normal.scale(f * invX, f * invY, f * invZ);
		return this;
	}

	@Override
	public TransformedInstance translate(double x, double y, double z) {
		setChanged();

		model.translate((float) x, (float) y, (float) z);
		return this;
	}

	@Override
	public TransformedInstance mulPose(Matrix4f pose) {
		setChanged();

		this.model.mul(pose);
		return this;
	}

	@Override
	public TransformedInstance mulNormal(Matrix3f normal) {
		setChanged();

		this.normal.mul(normal);
		return this;
	}
}
