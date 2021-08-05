package com.jozufozu.flywheel.fabric.util;

import com.mojang.math.Matrix4f;

public final class MatrixUtil {
	public static void set(Matrix4f self, Matrix4f mat) {
		self.m00 = mat.m00;
		self.m01 = mat.m01;
		self.m02 = mat.m02;
		self.m03 = mat.m03;
		self.m10 = mat.m10;
		self.m11 = mat.m11;
		self.m12 = mat.m12;
		self.m13 = mat.m13;
		self.m20 = mat.m20;
		self.m21 = mat.m21;
		self.m22 = mat.m22;
		self.m23 = mat.m23;
		self.m30 = mat.m30;
		self.m31 = mat.m31;
		self.m32 = mat.m32;
		self.m33 = mat.m33;
	}

	public static void multiplyBackward(Matrix4f self, Matrix4f other) {
		Matrix4f copy = other.copy();
		copy.multiply(self);
		set(self, copy);
	}

	public static void setTranslation(Matrix4f self, float x, float y, float z) {
		self.m00 = 1.0F;
		self.m11 = 1.0F;
		self.m22 = 1.0F;
		self.m33 = 1.0F;
		self.m03 = x;
		self.m13 = y;
		self.m23 = z;
	}
}
