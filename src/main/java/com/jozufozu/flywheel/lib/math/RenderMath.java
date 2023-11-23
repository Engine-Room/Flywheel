package com.jozufozu.flywheel.lib.math;

public final class RenderMath {
	/**
	 * Convert a signed byte into a signed, normalized float.
	 */
	public static float f(byte b) {
		return b / 127f;
	}

	/**
	 * Convert a signed, normalized float into a signed byte.
	 */
	public static byte nb(float f) {
		return (byte) (f * 127);
	}

	/**
	 * Convert an unsigned byte into an unsigned, normalized float.
	 */
	public static float uf(byte b) {
		return (float) (Byte.toUnsignedInt(b)) / 255f;
	}

	/**
	 * Convert an unsigned, normalized float into an unsigned byte.
	 */
	public static byte unb(float f) {
		return (byte) (int) (f * 255);
	}

	public static int nextPowerOf2(int a) {
		int h = Integer.highestOneBit(a);
		return (h == a) ? h : (h << 1);
	}

	public static float diffuseLight(float x, float y, float z, boolean shaded) {
		if (!shaded) {
			return 1f;
		}
		// FIXME: once we compile make sure this is correct.
		return Math.min(x * x + y * y * (3f + y) + z, 1f);
	}

	public static float diffuseLightNether(float x, float y, float z, boolean shaded) {
		if (!shaded) {
			return 0.9f;
		}
		return Math.min(x * x * 0.6f + y * y * 0.9f + z * z * 0.8f, 1f);
	}
}
