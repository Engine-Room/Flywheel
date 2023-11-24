package com.jozufozu.flywheel.lib.math;

import static org.joml.Math.fma;

import java.nio.ByteBuffer;

import org.joml.Math;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

public final class MatrixUtil {
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

	public static void write(Matrix4f matrix, ByteBuffer buf) {
		matrix.get(buf);
	}

	public static void writeUnsafe(Matrix4f matrix, long ptr) {
		MemoryUtil.memPutFloat(ptr, matrix.m00());
		MemoryUtil.memPutFloat(ptr + 4, matrix.m01());
		MemoryUtil.memPutFloat(ptr + 8, matrix.m02());
		MemoryUtil.memPutFloat(ptr + 12, matrix.m03());
		MemoryUtil.memPutFloat(ptr + 16, matrix.m10());
		MemoryUtil.memPutFloat(ptr + 20, matrix.m11());
		MemoryUtil.memPutFloat(ptr + 24, matrix.m12());
		MemoryUtil.memPutFloat(ptr + 28, matrix.m13());
		MemoryUtil.memPutFloat(ptr + 32, matrix.m20());
		MemoryUtil.memPutFloat(ptr + 36, matrix.m21());
		MemoryUtil.memPutFloat(ptr + 40, matrix.m22());
		MemoryUtil.memPutFloat(ptr + 44, matrix.m23());
		MemoryUtil.memPutFloat(ptr + 48, matrix.m30());
		MemoryUtil.memPutFloat(ptr + 52, matrix.m31());
		MemoryUtil.memPutFloat(ptr + 56, matrix.m32());
		MemoryUtil.memPutFloat(ptr + 60, matrix.m33());
	}

	public static void write(Matrix3f matrix, ByteBuffer buf) {
		matrix.get(buf);
	}

	public static void writeUnsafe(Matrix3f matrix, long ptr) {
		MemoryUtil.memPutFloat(ptr, matrix.m00());
		MemoryUtil.memPutFloat(ptr + 4, matrix.m01());
		MemoryUtil.memPutFloat(ptr + 8, matrix.m02());
		MemoryUtil.memPutFloat(ptr + 12, matrix.m10());
		MemoryUtil.memPutFloat(ptr + 16, matrix.m11());
		MemoryUtil.memPutFloat(ptr + 20, matrix.m12());
		MemoryUtil.memPutFloat(ptr + 24, matrix.m20());
		MemoryUtil.memPutFloat(ptr + 28, matrix.m21());
		MemoryUtil.memPutFloat(ptr + 32, matrix.m22());
	}

	/**
	 * Extracts the greatest scale factor across all axes from the given matrix.
	 *
	 * @param matrix The matrix to extract the scale from.
	 * @return The greatest scale factor across all axes.
	 */
	public static float extractScale(Matrix4f matrix) {
		float scaleSqrX = matrix.m00() * matrix.m00() + matrix.m10() * matrix.m10() + matrix.m20() * matrix.m20();
		float scaleSqrY = matrix.m01() * matrix.m01() + matrix.m11() * matrix.m11() + matrix.m21() * matrix.m21();
		float scaleSqrZ = matrix.m02() * matrix.m02() + matrix.m12() * matrix.m12() + matrix.m22() * matrix.m22();
		return Math.sqrt(Math.max(Math.max(scaleSqrX, scaleSqrY), scaleSqrZ));
	}
}
