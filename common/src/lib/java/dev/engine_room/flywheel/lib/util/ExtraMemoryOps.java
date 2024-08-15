package dev.engine_room.flywheel.lib.util;

import org.joml.Matrix3fc;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import org.joml.Vector2fc;
import org.joml.Vector3fc;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryUtil;

public final class ExtraMemoryOps {
	private ExtraMemoryOps() {
	}

	public static void put4x8(long ptr, int value) {
		MemoryUtil.memPutByte(ptr, (byte) (value & 0xFF));
		MemoryUtil.memPutByte(ptr + 1, (byte) (value >>> 8 & 0xFF));
		MemoryUtil.memPutByte(ptr + 2, (byte) (value >>> 16 & 0xFF));
		MemoryUtil.memPutByte(ptr + 3, (byte) (value >>> 24 & 0xFF));
	}

	public static void put2x16(long ptr, int value) {
		MemoryUtil.memPutShort(ptr, (short) (value & 0xFFFF));
		MemoryUtil.memPutShort(ptr + 2, (short) (value >>> 16 & 0xFFFF));
	}

	public static void putVector2f(long ptr, Vector2fc vector) {
		MemoryUtil.memPutFloat(ptr, vector.x());
		MemoryUtil.memPutFloat(ptr + 4, vector.y());
	}

	public static void putVector3f(long ptr, Vector3fc vector) {
		MemoryUtil.memPutFloat(ptr, vector.x());
		MemoryUtil.memPutFloat(ptr + 4, vector.y());
		MemoryUtil.memPutFloat(ptr + 8, vector.z());
	}

	public static void putVector4f(long ptr, Vector4fc vector) {
		MemoryUtil.memPutFloat(ptr, vector.x());
		MemoryUtil.memPutFloat(ptr + 4, vector.y());
		MemoryUtil.memPutFloat(ptr + 8, vector.z());
		MemoryUtil.memPutFloat(ptr + 12, vector.w());
	}

	public static void putQuaternionf(long ptr, Quaternionfc quaternion) {
		MemoryUtil.memPutFloat(ptr, quaternion.x());
		MemoryUtil.memPutFloat(ptr + 4, quaternion.y());
		MemoryUtil.memPutFloat(ptr + 8, quaternion.z());
		MemoryUtil.memPutFloat(ptr + 12, quaternion.w());
	}

	public static void putMatrix3f(long ptr, Matrix3fc matrix) {
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

	public static void putMatrix3fPadded(long ptr, Matrix3fc matrix) {
		MemoryUtil.memPutFloat(ptr, matrix.m00());
		MemoryUtil.memPutFloat(ptr + 4, matrix.m01());
		MemoryUtil.memPutFloat(ptr + 8, matrix.m02());
		MemoryUtil.memPutFloat(ptr + 12, 0.0f);
		MemoryUtil.memPutFloat(ptr + 16, matrix.m10());
		MemoryUtil.memPutFloat(ptr + 20, matrix.m11());
		MemoryUtil.memPutFloat(ptr + 24, matrix.m12());
		MemoryUtil.memPutFloat(ptr + 28, 0.0f);
		MemoryUtil.memPutFloat(ptr + 32, matrix.m20());
		MemoryUtil.memPutFloat(ptr + 36, matrix.m21());
		MemoryUtil.memPutFloat(ptr + 40, matrix.m22());
		MemoryUtil.memPutFloat(ptr + 44, 0.0f);
	}

	public static void putMatrix4f(long ptr, Matrix4fc matrix) {
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
}
