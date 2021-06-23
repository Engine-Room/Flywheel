package com.jozufozu.flywheel.util;

import java.util.function.Supplier;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;

public class RenderUtil {
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

	public static float[] writeMatrixStack(MatrixStack stack) {
		return writeMatrixStack(stack.last().pose(), stack.last().normal());
	}

	// GPUs want matrices in column major order.
	public static float[] writeMatrixStack(Matrix4f model, Matrix3f normal) {
		return new float[]{
				model.m00,
				model.m10,
				model.m20,
				model.m30,
				model.m01,
				model.m11,
				model.m21,
				model.m31,
				model.m02,
				model.m12,
				model.m22,
				model.m32,
				model.m03,
				model.m13,
				model.m23,
				model.m33,
				normal.m00,
				normal.m10,
				normal.m20,
				normal.m01,
				normal.m11,
				normal.m21,
				normal.m02,
				normal.m12,
				normal.m22,
		};
	}

	public static float[] writeMatrix(Matrix4f model) {
		return new float[]{
				model.m00,
				model.m10,
				model.m20,
				model.m30,
				model.m01,
				model.m11,
				model.m21,
				model.m31,
				model.m02,
				model.m12,
				model.m22,
				model.m32,
				model.m03,
				model.m13,
				model.m23,
				model.m33,
		};
	}

	public static Supplier<MatrixStack> rotateToFace(Direction facing) {
		return () -> {
			MatrixStack stack = new MatrixStack();
//			MatrixStacker.of(stack)
//					.centre()
//					.rotateY(AngleHelper.horizontalAngle(facing))
//					.rotateX(AngleHelper.verticalAngle(facing))
//					.unCentre();
			stack.last().pose().setTranslation(0.5f, 0.5f, 0.5f);
			stack.mulPose(Vector3f.YP.rotationDegrees(AngleHelper.horizontalAngle(facing)));
			stack.mulPose(Vector3f.XP.rotationDegrees(AngleHelper.verticalAngle(facing)));
			stack.translate(-0.5f, -0.5f, -0.5f);
			return stack;
		};
	}
}
