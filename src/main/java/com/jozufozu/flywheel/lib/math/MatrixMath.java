package com.jozufozu.flywheel.lib.math;

import static org.joml.Math.fma;

import org.joml.Math;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import com.mojang.blaze3d.vertex.PoseStack;

public final class MatrixMath {
	private MatrixMath() {
	}

	public static PoseStack copyPoseStack(PoseStack stack) {
		PoseStack copy = new PoseStack();
		copy.last()
				.pose()
				.set(stack.last()
						.pose());
		copy.last()
				.normal()
				.set(stack.last()
						.normal());
		return copy;
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
		float scaleSqrX = matrix.m00() * matrix.m00() + matrix.m01() * matrix.m01() + matrix.m02() * matrix.m02();
		float scaleSqrY = matrix.m10() * matrix.m10() + matrix.m11() * matrix.m11() + matrix.m12() * matrix.m12();
		float scaleSqrZ = matrix.m20() * matrix.m20() + matrix.m21() * matrix.m21() + matrix.m22() * matrix.m22();
		return Math.sqrt(Math.max(Math.max(scaleSqrX, scaleSqrY), scaleSqrZ));
	}

	/**
	 * Writes the frustum planes of the given projection matrix to the given buffer.<p>
	 * Uses a different format that is friendly towards an optimized instruction-parallel
	 * implementation of sphere-frustum intersection.<p>
	 * The format is as follows:<p>
	 * {@code vec4(nxX, pxX, nyX, pyX)}<br>
	 * {@code vec4(nxY, pxY, nyY, pyY)}<br>
	 * {@code vec4(nxZ, pxZ, nyZ, pyZ)}<br>
	 * {@code vec4(nxW, pxW, nyW, pyW)}<br>
	 * {@code vec2(nzX, pzX)}<br>
	 * {@code vec2(nzY, pzY)}<br>
	 * {@code vec2(nzZ, pzZ)}<br>
	 * {@code vec2(nzW, pzW)}<br>
	 * <p>
	 * Writes 96 bytes to the buffer.
	 *
	 * @param ptr The buffer to write the planes to.
	 * @param m   The projection matrix to compute the frustum planes for.
	 */
	public static void writePackedFrustumPlanes(long ptr, Matrix4f m) {
		float nxX, nxY, nxZ, nxW;
		float pxX, pxY, pxZ, pxW;
		float nyX, nyY, nyZ, nyW;
		float pyX, pyY, pyZ, pyW;
		float nzX, nzY, nzZ, nzW;
		float pzX, pzY, pzZ, pzW;

		float invl;
		nxX = m.m03() + m.m00();
		nxY = m.m13() + m.m10();
		nxZ = m.m23() + m.m20();
		nxW = m.m33() + m.m30();
		invl = Math.invsqrt(nxX * nxX + nxY * nxY + nxZ * nxZ);
		nxX *= invl;
		nxY *= invl;
		nxZ *= invl;
		nxW *= invl;

		pxX = m.m03() - m.m00();
		pxY = m.m13() - m.m10();
		pxZ = m.m23() - m.m20();
		pxW = m.m33() - m.m30();
		invl = Math.invsqrt(pxX * pxX + pxY * pxY + pxZ * pxZ);
		pxX *= invl;
		pxY *= invl;
		pxZ *= invl;
		pxW *= invl;

		nyX = m.m03() + m.m01();
		nyY = m.m13() + m.m11();
		nyZ = m.m23() + m.m21();
		nyW = m.m33() + m.m31();
		invl = Math.invsqrt(nyX * nyX + nyY * nyY + nyZ * nyZ);
		nyX *= invl;
		nyY *= invl;
		nyZ *= invl;
		nyW *= invl;

		pyX = m.m03() - m.m01();
		pyY = m.m13() - m.m11();
		pyZ = m.m23() - m.m21();
		pyW = m.m33() - m.m31();
		invl = Math.invsqrt(pyX * pyX + pyY * pyY + pyZ * pyZ);
		pyX *= invl;
		pyY *= invl;
		pyZ *= invl;
		pyW *= invl;

		nzX = m.m03() + m.m02();
		nzY = m.m13() + m.m12();
		nzZ = m.m23() + m.m22();
		nzW = m.m33() + m.m32();
		invl = Math.invsqrt(nzX * nzX + nzY * nzY + nzZ * nzZ);
		nzX *= invl;
		nzY *= invl;
		nzZ *= invl;
		nzW *= invl;

		pzX = m.m03() - m.m02();
		pzY = m.m13() - m.m12();
		pzZ = m.m23() - m.m22();
		pzW = m.m33() - m.m32();
		invl = Math.invsqrt(pzX * pzX + pzY * pzY + pzZ * pzZ);
		pzX *= invl;
		pzY *= invl;
		pzZ *= invl;
		pzW *= invl;

		MemoryUtil.memPutFloat(ptr, nxX);
		MemoryUtil.memPutFloat(ptr + 4, pxX);
		MemoryUtil.memPutFloat(ptr + 8, nyX);
		MemoryUtil.memPutFloat(ptr + 12, pyX);
		MemoryUtil.memPutFloat(ptr + 16, nxY);
		MemoryUtil.memPutFloat(ptr + 20, pxY);
		MemoryUtil.memPutFloat(ptr + 24, nyY);
		MemoryUtil.memPutFloat(ptr + 28, pyY);
		MemoryUtil.memPutFloat(ptr + 32, nxZ);
		MemoryUtil.memPutFloat(ptr + 36, pxZ);
		MemoryUtil.memPutFloat(ptr + 40, nyZ);
		MemoryUtil.memPutFloat(ptr + 44, pyZ);
		MemoryUtil.memPutFloat(ptr + 48, nxW);
		MemoryUtil.memPutFloat(ptr + 52, pxW);
		MemoryUtil.memPutFloat(ptr + 56, nyW);
		MemoryUtil.memPutFloat(ptr + 60, pyW);
		MemoryUtil.memPutFloat(ptr + 64, nzX);
		MemoryUtil.memPutFloat(ptr + 68, pzX);
		MemoryUtil.memPutFloat(ptr + 72, nzY);
		MemoryUtil.memPutFloat(ptr + 76, pzY);
		MemoryUtil.memPutFloat(ptr + 80, nzZ);
		MemoryUtil.memPutFloat(ptr + 84, pzZ);
		MemoryUtil.memPutFloat(ptr + 88, nzW);
		MemoryUtil.memPutFloat(ptr + 92, pzW);
	}
}
