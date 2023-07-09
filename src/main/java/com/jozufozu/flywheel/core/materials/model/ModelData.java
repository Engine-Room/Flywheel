package com.jozufozu.flywheel.core.materials.model;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import com.jozufozu.flywheel.core.materials.BasicData;
import com.jozufozu.flywheel.util.transform.Transform;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.util.Mth;

public class ModelData extends BasicData implements Transform<ModelData> {
	public final Matrix4f model = new Matrix4f();
	public final Matrix3f normal = new Matrix3f();

	public ModelData setTransform(PoseStack stack) {
		markDirty();

		this.model.set(stack.last().pose());
		this.normal.set(stack.last().normal());
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

		this.model.zero();
		this.normal.zero();
		return this;
	}

	public ModelData loadIdentity() {
		markDirty();

		this.model.identity();
		this.normal.identity();
		return this;
	}

	@Override
	public ModelData multiply(Quaternionf quaternion) {
		markDirty();

		model.rotate(quaternion);
		normal.rotate(quaternion);
		return this;
	}

	@Override
	public ModelData scale(float pX, float pY, float pZ) {
		markDirty();

		model.scale(pX, pY, pZ);
		if (pX == pY && pY == pZ) {
			if (pX > 0.0F) {
				return this;
			}

			normal.scale(-1.0F);
		}

		float f = 1.0F / pX;
		float f1 = 1.0F / pY;
		float f2 = 1.0F / pZ;
		float f3 = Mth.fastInvCubeRoot(f * f1 * f2);
		normal.scale(f3 * f, f3 * f1, f3 * f2);
		return this;
	}

	@Override
	public ModelData translate(double x, double y, double z) {
		markDirty();

		model.translate((float) x, (float) y, (float) z);
		return this;
	}

	@Override
	public ModelData mulPose(Matrix4f pose) {
		this.model.mul(pose);
		return this;
	}

	@Override
	public ModelData mulNormal(Matrix3f normal) {
		this.normal.mul(normal);
		return this;
	}
}
