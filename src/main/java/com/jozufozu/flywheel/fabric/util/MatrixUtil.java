package com.jozufozu.flywheel.fabric.util;

import net.minecraft.util.math.vector.Matrix4f;

public final class MatrixUtil {
	public static void set(Matrix4f self, Matrix4f mat) {
		self.a00 = mat.a00;
		self.a01 = mat.a01;
		self.a02 = mat.a02;
		self.a03 = mat.a03;
		self.a10 = mat.a10;
		self.a11 = mat.a11;
		self.a12 = mat.a12;
		self.a13 = mat.a13;
		self.a20 = mat.a20;
		self.a21 = mat.a21;
		self.a22 = mat.a22;
		self.a23 = mat.a23;
		self.a30 = mat.a30;
		self.a31 = mat.a31;
		self.a32 = mat.a32;
		self.a33 = mat.a33;
	}

	public static void multiplyBackward(Matrix4f self, Matrix4f other) {
		Matrix4f copy = other.copy();
		copy.multiply(self);
		set(self, copy);
	}

	public static void setTranslation(Matrix4f self, float x, float y, float z) {
		self.a00 = 1.0F;
		self.a11 = 1.0F;
		self.a22 = 1.0F;
		self.a33 = 1.0F;
		self.a03 = x;
		self.a13 = y;
		self.a23 = z;
	}
}
