package dev.engine_room.flywheel.lib.math;

import net.minecraft.util.Mth;

public final class DataPacker {
	private DataPacker() {
	}

	/**
	 * Pack a float as an unsigned, normalized byte.
	 */
	public static byte packNormU8(float f) {
		return (byte) (int) (Mth.clamp(f, 0.0f, 1.0f) * 255);
	}

	/**
	 * Unpack an unsigned, normalized byte to a float.
	 */
	public static float unpackNormU8(byte b) {
		return (float) (Byte.toUnsignedInt(b)) / 255f;
	}

	/**
	 * Pack a float as a signed, normalized byte.
	 */
	public static byte packNormI8(float f) {
		return (byte) (Mth.clamp(f, -1.0f, 1.0f) * 127);
	}

	/**
	 * Unpack a signed, normalized byte to a float.
	 */
	public static float unpackNormI8(byte b) {
		return (float) b / 127f;
	}

	/**
	 * Pack a float as an unsigned, normalized short.
	 */
	public static short packNormU16(float f) {
		return (short) (int) (Mth.clamp(f, 0.0f, 1.0f) * 65535);
	}

	/**
	 * Unpack an unsigned, normalized short to a float.
	 */
	public static float unpackNormU16(short s) {
		return (float) (Short.toUnsignedInt(s)) / 65535f;
	}

	/**
	 * Pack a float as a signed, normalized byte.
	 */
	public static short packNormI16(float f) {
		return (short) (Mth.clamp(f, -1.0f, 1.0f) * 32767);
	}

	/**
	 * Unpack a signed, normalized byte to a float.
	 */
	public static float unpackNormI16(short s) {
		return (float) s / 32767f;
	}
}
