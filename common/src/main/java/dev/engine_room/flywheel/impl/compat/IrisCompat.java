package dev.engine_room.flywheel.impl.compat;

public final class IrisCompat {
	public static final boolean ACTIVE = false;

	private IrisCompat() {
	}

	public static boolean isShaderPackInUse() {
		return false;
	}

	public static boolean isRenderingShadowPass() {
		return false;
	}
}
