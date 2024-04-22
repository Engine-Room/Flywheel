package com.jozufozu.flywheel.lib.util;

import java.lang.reflect.Field;
import java.util.function.BooleanSupplier;

import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

import com.jozufozu.flywheel.lib.internal.FlwLibXplat;
import com.mojang.logging.LogUtils;

public final class ShadersModHandler {
	private static final Logger LOGGER = LogUtils.getLogger();

	private static final String OPTIFINE_ROOT_PACKAGE = "net.optifine";

	private static final boolean IS_IRIS_LOADED;
	private static final boolean IS_OPTIFINE_INSTALLED;
	private static final InternalHandler INTERNAL_HANDLER;

	static {
		InternalHandler irisHandler = FlwLibXplat.INSTANCE
				.createIrisHandler();
		IS_IRIS_LOADED = irisHandler != null;

		Package optifinePackage = Package.getPackage(OPTIFINE_ROOT_PACKAGE);
		IS_OPTIFINE_INSTALLED = optifinePackage != null;

		// OptiFine and Iris/Oculus are assumed to be mutually exclusive

		if (IS_IRIS_LOADED) {
			LOGGER.debug("Iris detected.");
			INTERNAL_HANDLER = irisHandler;
		} else if (IS_OPTIFINE_INSTALLED) {
			LOGGER.debug("OptiFine detected.");
			INTERNAL_HANDLER = new OptifineHandler();
		} else {
			LOGGER.debug("No shaders mod detected.");
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
		}

		default boolean isRenderingShadowPass() {
			return false;
		}
	}

	// evil reflection
	private static class OptifineHandler implements InternalHandler {
		private final BooleanSupplier shadersEnabledSupplier;
		private final BooleanSupplier shadowPassSupplier;

		private OptifineHandler() {
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
					} catch (IllegalAccessException e) {
						return false;
					}
				};
			} catch (Exception e) {
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
					} catch (IllegalAccessException e) {
						return false;
					}
				};
			} catch (Exception e) {
				return () -> false;
			}
		}
	}
}
