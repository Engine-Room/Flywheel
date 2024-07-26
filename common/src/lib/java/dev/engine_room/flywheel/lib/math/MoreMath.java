package dev.engine_room.flywheel.lib.math;

public final class MoreMath {
	private MoreMath() {
	}

	/**
	 * The circumsphere of a cube has a radius of sqrt(3) / 2 * sideLength.
	 */
	public static final float SQRT_3_OVER_2 = (float) (Math.sqrt(3.0) / 2.0);

	public static int align16(int size) {
		return (size + 15) & ~15;
	}

	public static int align4(int size) {
		return (size + 3) & ~3;
	}

	public static int alignPot(int size, int to) {
		return (size + (to - 1)) & ~(to - 1);
	}

	public static int ceilingDiv(int numerator, int denominator) {
		return (numerator + denominator - 1) / denominator;
	}

	public static long ceilingDiv(long numerator, long denominator) {
		return (numerator + denominator - 1) / denominator;
	}

	public static long ceilLong(double d) {
		return (long) Math.ceil(d);
	}

	public static long ceilLong(float f) {
		return (long) Math.ceil(f);
	}
}
