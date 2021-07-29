package com.jozufozu.flywheel.util;

public class RenderMath {

	/**
	 * Convert a signed, normalized floating point value into a normalized byte.
	 */
	public static byte nb(float f) {
		return (byte) (f * 127);
	}

	/**
	 * Convert a signed byte into a normalized float.
	 */
	public static float f(byte b) {
		return b / 127f;
	}

	/**
	 * Convert an unsigned byte into a normalized float.
	 */
	public static float uf(byte b) {
		return (float) (Byte.toUnsignedInt(b)) / 255f;
	}

	/**
	 * Convert an unsigned, normalized float into an unsigned normalized byte.
	 */
	public static byte unb(float f) {
		return (byte) Math.floor(f * 255);
	}
}
