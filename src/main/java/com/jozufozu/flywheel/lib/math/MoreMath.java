package com.jozufozu.flywheel.lib.math;

public final class MoreMath {
	private MoreMath() {
	}

	/**
	 * The circumsphere of a cube has a radius of sqrt(3) / 2 * sideLength.
	 */
	public static final float SQRT_3_OVER_2 = (float) (Math.sqrt(3.0) / 2.0);

	public static int align16(int numToRound) {
		return (numToRound + 16 - 1) & -16;
	}

	public static int ceilingDiv(int numerator, int denominator) {
		return (numerator + denominator - 1) / denominator;
	}

	public static int numDigits(int number) {
		// cursed but allegedly the fastest algorithm, taken from https://www.baeldung.com/java-number-of-digits-in-int
		if (number < 100000) {
			if (number < 100) {
				if (number < 10) {
					return 1;
				} else {
					return 2;
				}
			} else {
				if (number < 1000) {
					return 3;
				} else {
					if (number < 10000) {
						return 4;
					} else {
						return 5;
					}
				}
			}
		} else {
			if (number < 10000000) {
				if (number < 1000000) {
					return 6;
				} else {
					return 7;
				}
			} else {
				if (number < 100000000) {
					return 8;
				} else {
					if (number < 1000000000) {
						return 9;
					} else {
						return 10;
					}
				}
			}
		}
	}
}
