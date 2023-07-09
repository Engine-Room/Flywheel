package com.jozufozu.flywheel.util;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;

public final class MatrixWrite {

	/**
	 * Write the contents of the matrix into sequential memory starting at the given address.
	 */
	public static void writeUnsafe(Matrix4f m, long ptr) {
		MemoryUtil.memPutFloat(ptr, m.m00());
		MemoryUtil.memPutFloat(ptr + 4, m.m01());
		MemoryUtil.memPutFloat(ptr + 8, m.m02());
		MemoryUtil.memPutFloat(ptr + 12, m.m03());
		MemoryUtil.memPutFloat(ptr + 16, m.m10());
		MemoryUtil.memPutFloat(ptr + 20, m.m11());
		MemoryUtil.memPutFloat(ptr + 24, m.m12());
		MemoryUtil.memPutFloat(ptr + 28, m.m13());
		MemoryUtil.memPutFloat(ptr + 32, m.m20());
		MemoryUtil.memPutFloat(ptr + 36, m.m21());
		MemoryUtil.memPutFloat(ptr + 40, m.m22());
		MemoryUtil.memPutFloat(ptr + 44, m.m23());
		MemoryUtil.memPutFloat(ptr + 48, m.m30());
		MemoryUtil.memPutFloat(ptr + 52, m.m31());
		MemoryUtil.memPutFloat(ptr + 56, m.m32());
		MemoryUtil.memPutFloat(ptr + 60, m.m33());
	}

	public static void write(Matrix4f m, VecBuffer buf) {
		buf.putFloat(m.m00());
		buf.putFloat(m.m01());
		buf.putFloat(m.m02());
		buf.putFloat(m.m03());
		buf.putFloat(m.m10());
		buf.putFloat(m.m11());
		buf.putFloat(m.m12());
		buf.putFloat(m.m13());
		buf.putFloat(m.m20());
		buf.putFloat(m.m21());
		buf.putFloat(m.m22());
		buf.putFloat(m.m23());
		buf.putFloat(m.m30());
		buf.putFloat(m.m31());
		buf.putFloat(m.m32());
		buf.putFloat(m.m33());
	}

	/**
	 * Write the contents of the matrix into sequential memory starting at the given address.
	 */
	public static void writeUnsafe(Matrix3f m, long ptr) {
		MemoryUtil.memPutFloat(ptr, m.m00);
		MemoryUtil.memPutFloat(ptr + 4, m.m01);
		MemoryUtil.memPutFloat(ptr + 8, m.m02);
		MemoryUtil.memPutFloat(ptr + 12, m.m10);
		MemoryUtil.memPutFloat(ptr + 16, m.m11);
		MemoryUtil.memPutFloat(ptr + 20, m.m12);
		MemoryUtil.memPutFloat(ptr + 24, m.m20);
		MemoryUtil.memPutFloat(ptr + 28, m.m21);
		MemoryUtil.memPutFloat(ptr + 32, m.m22);
	}

	public static void write(Matrix3f m, VecBuffer buf) {
		buf.putFloat(m.m00);
		buf.putFloat(m.m01);
		buf.putFloat(m.m02);
		buf.putFloat(m.m10);
		buf.putFloat(m.m11);
		buf.putFloat(m.m12);
		buf.putFloat(m.m20);
		buf.putFloat(m.m21);
		buf.putFloat(m.m22);
	}
}
