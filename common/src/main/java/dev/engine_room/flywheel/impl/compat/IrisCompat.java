package dev.engine_room.flywheel.impl.compat;

import dev.engine_room.flywheel.impl.FlwImpl;
import dev.engine_room.flywheel.impl.FlwImplXplat;
import net.irisshaders.iris.api.v0.IrisApi;

public final class IrisCompat {
	public static final boolean ACTIVE = FlwImplXplat.INSTANCE.useIrisCompat();

	static {
		if (ACTIVE) {
			FlwImpl.LOGGER.debug("Detected Iris");
		}
	}

	private IrisCompat() {
	}

	public static boolean isShaderPackInUse() {
		if (!ACTIVE) {
			return false;
		}

		return Internals.isShaderPackInUse();
	}

	public static boolean isRenderingShadowPass() {
		if (!ACTIVE) {
			return false;
		}

		return Internals.isRenderingShadowPass();
	}

	private static final class Internals {
		static boolean isShaderPackInUse() {
			return IrisApi.getInstance()
					.isShaderPackInUse();
		}

		static boolean isRenderingShadowPass() {
			return IrisApi.getInstance()
					.isRenderingShadowPass();
		}
	}
}
