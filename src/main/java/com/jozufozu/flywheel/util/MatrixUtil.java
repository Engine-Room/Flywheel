package com.jozufozu.flywheel.util;

import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.mixin.matrix.Matrix3fAccessor;
import com.jozufozu.flywheel.mixin.matrix.Matrix4fAccessor;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;

public class MatrixUtil {
	public static void write(Matrix4f matrix, ByteBuffer buf) {
		Matrix4fAccessor m = (Matrix4fAccessor) (Object) matrix;
		buf.putFloat(m.flywheel$m00());
		buf.putFloat(m.flywheel$m10());
		buf.putFloat(m.flywheel$m20());
		buf.putFloat(m.flywheel$m30());
		buf.putFloat(m.flywheel$m01());
		buf.putFloat(m.flywheel$m11());
		buf.putFloat(m.flywheel$m21());
		buf.putFloat(m.flywheel$m31());
		buf.putFloat(m.flywheel$m02());
		buf.putFloat(m.flywheel$m12());
		buf.putFloat(m.flywheel$m22());
		buf.putFloat(m.flywheel$m32());
		buf.putFloat(m.flywheel$m03());
		buf.putFloat(m.flywheel$m13());
		buf.putFloat(m.flywheel$m23());
		buf.putFloat(m.flywheel$m33());
	}

	public static void writeUnsafe(Matrix4f matrix, long ptr) {
		Matrix4fAccessor m = (Matrix4fAccessor) (Object) matrix;
		MemoryUtil.memPutFloat(ptr, m.flywheel$m00());
		MemoryUtil.memPutFloat(ptr + 4, m.flywheel$m10());
		MemoryUtil.memPutFloat(ptr + 8, m.flywheel$m20());
		MemoryUtil.memPutFloat(ptr + 12, m.flywheel$m30());
		MemoryUtil.memPutFloat(ptr + 16, m.flywheel$m01());
		MemoryUtil.memPutFloat(ptr + 20, m.flywheel$m11());
		MemoryUtil.memPutFloat(ptr + 24, m.flywheel$m21());
		MemoryUtil.memPutFloat(ptr + 28, m.flywheel$m31());
		MemoryUtil.memPutFloat(ptr + 32, m.flywheel$m02());
		MemoryUtil.memPutFloat(ptr + 36, m.flywheel$m12());
		MemoryUtil.memPutFloat(ptr + 40, m.flywheel$m22());
		MemoryUtil.memPutFloat(ptr + 44, m.flywheel$m32());
		MemoryUtil.memPutFloat(ptr + 48, m.flywheel$m03());
		MemoryUtil.memPutFloat(ptr + 52, m.flywheel$m13());
		MemoryUtil.memPutFloat(ptr + 56, m.flywheel$m23());
		MemoryUtil.memPutFloat(ptr + 60, m.flywheel$m33());
	}

	public static void write(Matrix3f matrix, ByteBuffer buf) {
		Matrix3fAccessor m = (Matrix3fAccessor) (Object) matrix;
		buf.putFloat(m.flywheel$m00());
		buf.putFloat(m.flywheel$m10());
		buf.putFloat(m.flywheel$m20());
		buf.putFloat(m.flywheel$m01());
		buf.putFloat(m.flywheel$m11());
		buf.putFloat(m.flywheel$m21());
		buf.putFloat(m.flywheel$m02());
		buf.putFloat(m.flywheel$m12());
		buf.putFloat(m.flywheel$m22());
	}

	public static void store(Matrix4f matrix, com.jozufozu.flywheel.util.joml.Matrix4f jomlMatrix) {
		Matrix4fAccessor m = (Matrix4fAccessor) (Object) matrix;
		jomlMatrix.set(
				m.flywheel$m00(), m.flywheel$m10(), m.flywheel$m20(), m.flywheel$m30(),
				m.flywheel$m01(), m.flywheel$m11(), m.flywheel$m21(), m.flywheel$m31(),
				m.flywheel$m02(), m.flywheel$m12(), m.flywheel$m22(), m.flywheel$m32(),
				m.flywheel$m03(), m.flywheel$m13(), m.flywheel$m23(), m.flywheel$m33()
		);
	}

	public static com.jozufozu.flywheel.util.joml.Matrix4f toJoml(Matrix4f matrix) {
		Matrix4fAccessor m = (Matrix4fAccessor) (Object) matrix;
		return new com.jozufozu.flywheel.util.joml.Matrix4f(
				m.flywheel$m00(), m.flywheel$m10(), m.flywheel$m20(), m.flywheel$m30(),
				m.flywheel$m01(), m.flywheel$m11(), m.flywheel$m21(), m.flywheel$m31(),
				m.flywheel$m02(), m.flywheel$m12(), m.flywheel$m22(), m.flywheel$m32(),
				m.flywheel$m03(), m.flywheel$m13(), m.flywheel$m23(), m.flywheel$m33()
		);
	}

	public static void writeUnsafe(Matrix3f matrix, long ptr) {
		Matrix3fAccessor m = (Matrix3fAccessor) (Object) matrix;
		MemoryUtil.memPutFloat(ptr, m.flywheel$m00());
		MemoryUtil.memPutFloat(ptr + 4, m.flywheel$m10());
		MemoryUtil.memPutFloat(ptr + 8, m.flywheel$m20());
		MemoryUtil.memPutFloat(ptr + 12, m.flywheel$m01());
		MemoryUtil.memPutFloat(ptr + 16, m.flywheel$m11());
		MemoryUtil.memPutFloat(ptr + 20, m.flywheel$m21());
		MemoryUtil.memPutFloat(ptr + 24, m.flywheel$m02());
		MemoryUtil.memPutFloat(ptr + 28, m.flywheel$m12());
		MemoryUtil.memPutFloat(ptr + 32, m.flywheel$m22());
	}

	public static void store(Matrix3f matrix, com.jozufozu.flywheel.util.joml.Matrix3f jomlMatrix) {
		Matrix3fAccessor m = (Matrix3fAccessor) (Object) matrix;
		jomlMatrix.set(
				m.flywheel$m00(), m.flywheel$m10(), m.flywheel$m20(),
				m.flywheel$m01(), m.flywheel$m11(), m.flywheel$m21(),
				m.flywheel$m02(), m.flywheel$m12(), m.flywheel$m22()
		);
	}

	public static com.jozufozu.flywheel.util.joml.Matrix3f toJoml(Matrix3f matrix) {
		Matrix3fAccessor m = (Matrix3fAccessor) (Object) matrix;
		return new com.jozufozu.flywheel.util.joml.Matrix3f(
				m.flywheel$m00(), m.flywheel$m10(), m.flywheel$m20(),
				m.flywheel$m01(), m.flywheel$m11(), m.flywheel$m21(),
				m.flywheel$m02(), m.flywheel$m12(), m.flywheel$m22()
		);
	}
}
