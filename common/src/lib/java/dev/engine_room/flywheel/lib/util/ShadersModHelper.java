package dev.engine_room.flywheel.lib.util;

import dev.engine_room.flywheel.lib.internal.FlwLibLink;

public final class ShadersModHelper {
	public static final boolean IS_IRIS_LOADED = FlwLibLink.INSTANCE.isIrisLoaded();
	public static final boolean IS_OPTIFINE_INSTALLED = FlwLibLink.INSTANCE.isOptifineInstalled();

	private ShadersModHelper() {
	}

	public static boolean isShaderPackInUse() {
		return FlwLibLink.INSTANCE.isShaderPackInUse();
	}

	public static boolean isRenderingShadowPass() {
		return FlwLibLink.INSTANCE.isRenderingShadowPass();
	}
}
