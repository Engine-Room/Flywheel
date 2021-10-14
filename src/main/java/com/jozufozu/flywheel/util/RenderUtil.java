package com.jozufozu.flywheel.util;

import java.util.function.Supplier;

import com.jozufozu.flywheel.fabric.helper.Matrix4fHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import net.minecraft.core.Direction;

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

	public static Supplier<PoseStack> rotateToFace(Direction facing) {
		return () -> {
			PoseStack stack = new PoseStack();
			//			MatrixStacker.of(stack)
			//					.centre()
			//					.rotateY(AngleHelper.horizontalAngle(facing))
			//					.rotateX(AngleHelper.verticalAngle(facing))
			//					.unCentre();
			Matrix4fHelper.setTranslation(stack.last()
					.pose(),
					0.5f, 0.5f, 0.5f);
			stack.mulPose(Vector3f.YP.rotationDegrees(AngleHelper.horizontalAngle(facing)));
			stack.mulPose(Vector3f.XP.rotationDegrees(AngleHelper.verticalAngle(facing)));
			stack.translate(-0.5f, -0.5f, -0.5f);
			return stack;
		};
	}
}
