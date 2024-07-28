package dev.engine_room.flywheel.lib.math;

import static org.joml.Math.fma;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

public final class MatrixMath {
	private MatrixMath() {
	}

	public static float transformPositionX(Matrix4f matrix, float x, float y, float z) {
		return fma(matrix.m00(), x, fma(matrix.m10(), y, fma(matrix.m20(), z, matrix.m30())));
	}

	public static float transformPositionY(Matrix4f matrix, float x, float y, float z) {
		return fma(matrix.m01(), x, fma(matrix.m11(), y, fma(matrix.m21(), z, matrix.m31())));
	}

	public static float transformPositionZ(Matrix4f matrix, float x, float y, float z) {
		return fma(matrix.m02(), x, fma(matrix.m12(), y, fma(matrix.m22(), z, matrix.m32())));
	}

	public static float transformNormalX(Matrix3f matrix, float x, float y, float z) {
		return fma(matrix.m00(), x, fma(matrix.m10(), y, matrix.m20() * z));
	}

	public static float transformNormalY(Matrix3f matrix, float x, float y, float z) {
		return fma(matrix.m01(), x, fma(matrix.m11(), y, matrix.m21() * z));
	}

	public static float transformNormalZ(Matrix3f matrix, float x, float y, float z) {
		return fma(matrix.m02(), x, fma(matrix.m12(), y, matrix.m22() * z));
	}
}
