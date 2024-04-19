package com.jozufozu.flywheel.lib.util;

import java.lang.reflect.Field;
import java.util.function.BooleanSupplier;

import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

import com.jozufozu.flywheel.platform.ClientPlatform;
import com.mojang.logging.LogUtils;

public final class ShadersModHandler {
	private static final Logger LOGGER = LogUtils.getLogger();

	private static final String OPTIFINE_ROOT_PACKAGE = "net.optifine";

	private static final boolean IS_IRIS_LOADED;
	private static final boolean IS_OPTIFINE_INSTALLED;
	private static final InternalHandler INTERNAL_HANDLER;

	static {
		Package optifinePackage = Package.getPackage(OPTIFINE_ROOT_PACKAGE);
		IS_OPTIFINE_INSTALLED = optifinePackage != null;

		var irisOculusHandler = ClientPlatform.getInstance()
				.createIrisOculusHandlerIfPresent();
		IS_IRIS_LOADED = irisOculusHandler != null;

		// OptiFine and Iris/Oculus are assumed to be mutually exclusive

		if (IS_OPTIFINE_INSTALLED) {
			LOGGER.info("Optifine detected.");
			INTERNAL_HANDLER = new Optifine();
		} else if (IS_IRIS_LOADED) {
			LOGGER.info("Iris detected.");
			INTERNAL_HANDLER = irisOculusHandler;
		} else {
			LOGGER.info("No shaders mod detected.");
			INTERNAL_HANDLER = new InternalHandler() {};
		}
	}

	private ShadersModHandler() {
	}

	public static boolean isIrisLoaded() {
		return IS_IRIS_LOADED;
	}

	public static boolean isOptifineInstalled() {
		return IS_OPTIFINE_INSTALLED;
	}

	public static boolean isShaderPackInUse() {
		return INTERNAL_HANDLER.isShaderPackInUse();
	}

	public static boolean isRenderingShadowPass() {
		return INTERNAL_HANDLER.isRenderingShadowPass();
	}

	@ApiStatus.Internal
	public static void init() {
	}

	@ApiStatus.Internal
	public interface InternalHandler {
		default boolean isShaderPackInUse() {
			return false;
		};

		default boolean isRenderingShadowPass() {
			return false;
		};
	}

	// evil reflection
	private static class Optifine implements InternalHandler {
		private final BooleanSupplier shadersEnabledSupplier;
		private final BooleanSupplier shadowPassSupplier;

		Optifine() {
			shadersEnabledSupplier = createShadersEnabledSupplier();
			shadowPassSupplier = createShadowPassSupplier();
		}

		@Override
		public boolean isShaderPackInUse() {
			return shadersEnabledSupplier.getAsBoolean();
		}

		@Override
		public boolean isRenderingShadowPass() {
			return shadowPassSupplier.getAsBoolean();
		}

		private static BooleanSupplier createShadersEnabledSupplier() {
			try {
				Class<?> ofShaders = Class.forName("net.optifine.shaders.Shaders");
				Field field = ofShaders.getDeclaredField("shaderPackLoaded");
				field.setAccessible(true);
				return () -> {
					try {
						return field.getBoolean(null);
					} catch (IllegalAccessException ignored) {
						return false;
					}
				};
			} catch (Exception ignored) {
				return () -> false;
			}
		}

		private static BooleanSupplier createShadowPassSupplier() {
			try {
				Class<?> ofShaders = Class.forName("net.optifine.shaders.Shaders");
				Field field = ofShaders.getDeclaredField("isShadowPass");
				field.setAccessible(true);
				return () -> {
					try {
						return field.getBoolean(null);
					} catch (IllegalAccessException ignored) {
						return false;
					}
				};
			} catch (Exception ignored) {
				return () -> false;
			}
		}
	}
}
