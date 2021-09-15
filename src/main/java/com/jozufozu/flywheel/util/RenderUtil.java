package com.jozufozu.flywheel.util;

import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.core.Direction;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

public class RenderUtil {

	private static final Matrix4f IDENTITY = new Matrix4f();
	static {
		IDENTITY.setIdentity();
	}

	public static Matrix4f getIdentity() {
		return IDENTITY;
	}

	public static Matrix4f copyIdentity() {
		return IDENTITY.copy();
	}

	public static int nextPowerOf2(int a) {
		int h = Integer.highestOneBit(a);
		return (h == a) ? h : (h << 1);
	}

	public static boolean isPowerOf2(int n) {
		int b = n & (n - 1);
		return b == 0 && n != 0;
	}

	public static double lengthSqr(double x, double y, double z) {
		return x * x + y * y + z * z;
	}

	public static double length(double x, double y, double z) {
		return Math.sqrt(lengthSqr(x, y, z));
	}

	public static float[] writeMatrixStack(PoseStack stack) {
		return writeMatrixStack(stack.last()
										.pose(), stack.last()
										.normal());
	}

	// GPUs want matrices in column major order.
	public static float[] writeMatrixStack(Matrix4f model, Matrix3f normal) {
		return new float[]{model.m00, model.m10, model.m20, model.m30, model.m01, model.m11, model.m21, model.m31, model.m02, model.m12, model.m22, model.m32, model.m03, model.m13, model.m23, model.m33, normal.m00, normal.m10, normal.m20, normal.m01, normal.m11, normal.m21, normal.m02, normal.m12, normal.m22,};
	}

	public static float[] writeMatrix(Matrix4f model) {
		return new float[]{model.m00, model.m10, model.m20, model.m30, model.m01, model.m11, model.m21, model.m31, model.m02, model.m12, model.m22, model.m32, model.m03, model.m13, model.m23, model.m33,};
	}

	public static Supplier<PoseStack> rotateToFace(Direction facing) {
		return () -> {
			PoseStack stack = new PoseStack();
			//			MatrixStacker.of(stack)
			//					.centre()
			//					.rotateY(AngleHelper.horizontalAngle(facing))
			//					.rotateX(AngleHelper.verticalAngle(facing))
			//					.unCentre();
			stack.last()
					.pose()
					.setTranslation(0.5f, 0.5f, 0.5f);
			stack.mulPose(Vector3f.YP.rotationDegrees(AngleHelper.horizontalAngle(facing)));
			stack.mulPose(Vector3f.XP.rotationDegrees(AngleHelper.verticalAngle(facing)));
			stack.translate(-0.5f, -0.5f, -0.5f);
			return stack;
		};
	}
}
