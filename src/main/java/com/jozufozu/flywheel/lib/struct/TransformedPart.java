package com.jozufozu.flywheel.lib.struct;

import com.jozufozu.flywheel.api.instancer.Handle;
import com.jozufozu.flywheel.lib.transform.Transform;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;

import net.minecraft.util.Mth;

public class TransformedPart extends ColoredLitPart implements Transform<TransformedPart> {
	private static final Matrix4f EMPTY_MATRIX_4f = new Matrix4f();
	private static final Matrix3f EMPTY_MATRIX_3f = new Matrix3f();

	public final Matrix4f model = new Matrix4f();
	public final Matrix3f normal = new Matrix3f();

	public TransformedPart(Handle handle) {
		super(StructTypes.TRANSFORMED, handle);
	}

	public TransformedPart setTransform(PoseStack stack) {
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
	public TransformedPart setEmptyTransform() {
		setChanged();

		this.model.load(EMPTY_MATRIX_4f);
		this.normal.load(EMPTY_MATRIX_3f);
		return this;
	}

	public TransformedPart loadIdentity() {
		setChanged();

		this.model.setIdentity();
		this.normal.setIdentity();
		return this;
	}

	@Override
	public TransformedPart multiply(Quaternion quaternion) {
		setChanged();

		model.multiply(quaternion);
		normal.mul(quaternion);
		return this;
	}

	@Override
	public TransformedPart scale(float pX, float pY, float pZ) {
		setChanged();

		model.multiply(Matrix4f.createScaleMatrix(pX, pY, pZ));
		if (pX == pY && pY == pZ) {
			if (pX > 0.0F) {
				return this;
			}

			normal.mul(-1.0F);
			return this;
		}

		float f = 1.0F / pX;
		float f1 = 1.0F / pY;
		float f2 = 1.0F / pZ;
		float f3 = Mth.fastInvCubeRoot(Math.abs(f * f1 * f2));
		normal.mul(Matrix3f.createScaleMatrix(f3 * f, f3 * f1, f3 * f2));
		return this;
	}

	@Override
	public TransformedPart translate(double x, double y, double z) {
		setChanged();

		model.multiplyWithTranslation((float) x, (float) y, (float) z);
		return this;
	}

	@Override
	public TransformedPart mulPose(Matrix4f pose) {
		this.model.multiply(pose);
		return this;
	}

	@Override
	public TransformedPart mulNormal(Matrix3f normal) {
		this.normal.mul(normal);
		return this;
	}

	@Override
	public TransformedPart copy(Handle handle) {
		var out = new TransformedPart(handle);
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
