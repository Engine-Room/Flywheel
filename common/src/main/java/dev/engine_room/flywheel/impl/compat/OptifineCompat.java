package dev.engine_room.flywheel.impl.compat;

import java.lang.reflect.Field;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.impl.FlwImpl;

public final class OptifineCompat {
	private static final String OPTIFINE_ROOT_PACKAGE = "net.optifine";

	public static final boolean IS_INSTALLED;
	@Nullable
	private static final Field SHADER_PACK_LOADED_FIELD;
	@Nullable
	private static final Field IS_SHADOW_PASS_FIELD;

	static {
		Field shaderPackLoadedField = null;
		Field isShadowPassField = null;

		Package optifinePackage = Package.getPackage(OPTIFINE_ROOT_PACKAGE);
		IS_INSTALLED = optifinePackage != null;

		if (IS_INSTALLED) {
			try {
				Class<?> shadersClass = Class.forName("net.optifine.shaders.Shaders");
				shaderPackLoadedField = shadersClass.getDeclaredField("shaderPackLoaded");
				shaderPackLoadedField.setAccessible(true);
				isShadowPassField = shadersClass.getDeclaredField("isShadowPass");
				isShadowPassField.setAccessible(true);
			} catch (Exception e) {
				FlwImpl.LOGGER.debug("Failed to access OptiFine internals", e);
			}
		}

		SHADER_PACK_LOADED_FIELD = shaderPackLoadedField;
		IS_SHADOW_PASS_FIELD = isShadowPassField;
	}

	static {
		if (IS_INSTALLED) {
			FlwImpl.LOGGER.debug("Detected OptiFine");
		}
	}

	private OptifineCompat() {
	}

	public static boolean isShaderPackInUse() {
		if (SHADER_PACK_LOADED_FIELD == null) {
			return false;
		}

		try {
			return SHADER_PACK_LOADED_FIELD.getBoolean(null);
		} catch (IllegalAccessException e) {
			return false;
		}
	}

	public static boolean isRenderingShadowPass() {
		if (IS_SHADOW_PASS_FIELD == null) {
			return false;
		}

		try {
			return IS_SHADOW_PASS_FIELD.getBoolean(null);
		} catch (IllegalAccessException e) {
			return false;
		}
	}
}
