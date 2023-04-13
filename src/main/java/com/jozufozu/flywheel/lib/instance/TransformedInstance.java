package com.jozufozu.flywheel.lib.instance;

import com.jozufozu.flywheel.api.instance.InstanceHandle;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.lib.transform.Transform;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;

import net.minecraft.util.Mth;

public class TransformedInstance extends ColoredLitInstance implements Transform<TransformedInstance> {
	private static final Matrix4f EMPTY_MATRIX_4f = new Matrix4f();
	private static final Matrix3f EMPTY_MATRIX_3f = new Matrix3f();

	public final Matrix4f model = new Matrix4f();
	public final Matrix3f normal = new Matrix3f();

	public TransformedInstance(InstanceType<? extends TransformedInstance> type, InstanceHandle handle) {
		super(type, handle);
	}

	public TransformedInstance setTransform(PoseStack stack) {
		setChanged();

		this.model.load(stack.last()
				.pose());
		this.normal.load(stack.last()
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

		this.model.load(EMPTY_MATRIX_4f);
		this.normal.load(EMPTY_MATRIX_3f);
		return this;
	}

	public TransformedInstance loadIdentity() {
		setChanged();

		this.model.setIdentity();
		this.normal.setIdentity();
		return this;
	}

	@Override
	public TransformedInstance multiply(Quaternion quaternion) {
		setChanged();

		model.multiply(quaternion);
		normal.mul(quaternion);
		return this;
	}

	@Override
	public TransformedInstance scale(float x, float y, float z) {
		setChanged();

		model.multiply(Matrix4f.createScaleMatrix(x, y, z));

		if (x == y && y == z) {
			if (x < 0.0f) {
				normal.mul(-1.0f);
			}

			return this;
		}

		float invX = 1.0f / x;
		float invY = 1.0f / y;
		float invZ = 1.0f / z;
		float f = Mth.fastInvCubeRoot(Math.abs(invX * invY * invZ));
		normal.mul(Matrix3f.createScaleMatrix(f * invX, f * invY, f * invZ));
		return this;
	}

	@Override
	public TransformedInstance translate(double x, double y, double z) {
		setChanged();

		model.multiplyWithTranslation((float) x, (float) y, (float) z);
		return this;
	}

	@Override
	public TransformedInstance mulPose(Matrix4f pose) {
		this.model.multiply(pose);
		return this;
	}

	@Override
	public TransformedInstance mulNormal(Matrix3f normal) {
		this.normal.mul(normal);
		return this;
	}

	@Override
	public TransformedInstance copy(InstanceHandle handle) {
		var out = InstanceTypes.TRANSFORMED.create(handle);
		out.model.load(this.model);
		out.normal.load(this.normal);
		out.r = this.r;
		out.g = this.g;
		out.b = this.b;
		out.a = this.a;
		out.blockLight = this.blockLight;
		out.skyLight = this.skyLight;
		return out;
	}
}
