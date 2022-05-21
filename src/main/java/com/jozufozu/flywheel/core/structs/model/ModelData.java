package com.jozufozu.flywheel.core.structs.model;

import com.jozufozu.flywheel.core.structs.BasicData;
import com.jozufozu.flywheel.util.transform.Transform;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;

import net.minecraft.util.Mth;

public class ModelData extends BasicData implements Transform<ModelData> {
	private static final Matrix4f EMPTY_MATRIX_4f = new Matrix4f();
	private static final Matrix3f EMPTY_MATRIX_3f = new Matrix3f();

	public final Matrix4f model = new Matrix4f();
	public final Matrix3f normal = new Matrix3f();

	public ModelData setTransform(PoseStack stack) {
		markDirty();

		this.model.load(stack.last().pose());
		this.normal.load(stack.last().normal());
		return this;
	}

	/**
	 * Sets the transform matrices to be all zeros.
	 *
	 * <p>
	 *     This will allow the gpu to quickly discard all geometry for this instance, effectively "turning it off".
	 * </p>
	 */
	public ModelData setEmptyTransform() {
		markDirty();

		this.model.load(EMPTY_MATRIX_4f);
		this.normal.load(EMPTY_MATRIX_3f);
		return this;
	}

	public ModelData loadIdentity() {
		markDirty();

		this.model.setIdentity();
		this.normal.setIdentity();
		return this;
	}

	@Override
	public ModelData multiply(Quaternion quaternion) {
		markDirty();

		model.multiply(quaternion);
		normal.mul(quaternion);
		return this;
	}

	@Override
	public ModelData scale(float pX, float pY, float pZ) {
		markDirty();

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
	public ModelData translate(double x, double y, double z) {
		markDirty();

		model.multiplyWithTranslation((float) x, (float) y, (float) z);
		return this;
	}

	@Override
	public ModelData mulPose(Matrix4f pose) {
		this.model.multiply(pose);
		return this;
	}

	@Override
	public ModelData mulNormal(Matrix3f normal) {
		this.normal.mul(normal);
		return this;
	}
}
