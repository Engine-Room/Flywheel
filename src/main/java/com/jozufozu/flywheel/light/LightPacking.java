package com.jozufozu.flywheel.light;

/**
 * Utility class for bit-twiddling light.
 */
public class LightPacking {
	public static int getBlock(short packed) {
		return (packed >> 4) & 0xF;
	}

	public static int getSky(short packed) {
		return (packed >> 12) & 0xF;
	}

	public static byte packLightNibbles(byte block, byte sky) {
		return (byte) (block | (sky << 4));
	}

	public static int getBlock(byte packed) {
		return packed & 0xF;
	}

	public static int getSky(byte packed) {
		return (packed >> 4) & 0xF;
	}
}
