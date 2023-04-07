package com.jozufozu.flywheel.lib.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.BooleanSupplier;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraftforge.fml.ModList;

public final class ShadersModHandler {
	private static final Logger LOGGER = LogUtils.getLogger();

	public static final String OPTIFINE_ROOT_PACKAGE = "net.optifine";
	public static final String OPTIFINE_SHADER_PACKAGE = "net.optifine.shaders";

	private static final boolean IS_OCULUS_LOADED;
	private static final boolean IS_OPTIFINE_INSTALLED;
	private static final InternalHandler INTERNAL_HANDLER;

	static {
		Package optifinePackage = Package.getPackage(OPTIFINE_ROOT_PACKAGE);
		IS_OPTIFINE_INSTALLED = optifinePackage != null;
		IS_OCULUS_LOADED = ModList.get()
				.isLoaded("oculus");

		// optfine and oculus are assumed to be mutually exclusive

		if (IS_OPTIFINE_INSTALLED) {
			LOGGER.info("Optifine detected.");
			INTERNAL_HANDLER = new Optifine();
		} else if (IS_OCULUS_LOADED) {
			LOGGER.info("Oculus detected.");
			INTERNAL_HANDLER = new Oculus();
		} else {
			LOGGER.info("No shaders mod detected.");
			INTERNAL_HANDLER = new InternalHandler() {};
		}
	}

	public static boolean isOculusLoaded() {
		return IS_OCULUS_LOADED;
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

	private ShadersModHandler() {
	}

	private interface InternalHandler {
		default boolean isShaderPackInUse() {
			return false;
		};

		default boolean isRenderingShadowPass() {
			return false;
		};
	}

	// simple, lovely api calls
	private static class Oculus implements InternalHandler {
		@Override
		public boolean isShaderPackInUse() {
			return IrisApi.getInstance()
					.isShaderPackInUse();
		}

		@Override
		public boolean isRenderingShadowPass() {
			return IrisApi.getInstance()
					.isRenderingShadowPass();
		}
	}

	// evil reflection
	private static class Optifine implements InternalHandler {
		private final BooleanSupplier shadersEnabledSupplier;
		private final BooleanSupplier shadowPassSupplier;
		private final FrustumConstructor shadowFrustumConstructor;

		Optifine() {
			shadersEnabledSupplier = createShadersEnabledSupplier();
			shadowPassSupplier = createShadowPassSupplier();
			shadowFrustumConstructor = createShadowFrustumConstructor();
		}

		@Override
		public boolean isShaderPackInUse() {
			return shadersEnabledSupplier.getAsBoolean();
		}

		@Override
		public boolean isRenderingShadowPass() {
			return shadowPassSupplier.getAsBoolean();
		}

		@Nullable
		public Frustum createShadowFrustum(Camera camera, float partialTicks) {
			var frustum = shadowFrustumConstructor.create(camera, partialTicks);
			if (frustum != null) {
				var position = camera.getPosition();
				frustum.prepare(position.x, position.y, position.z);
			}
			return frustum;
		}

		private static FrustumConstructor createShadowFrustumConstructor() {
			try {
				Class<?> ofShaders = Class.forName("net.optifine.shaders.ShadersRender");
				Method method = ofShaders.getDeclaredMethod("makeShadowFrustum", Camera.class, Float.TYPE);
				method.setAccessible(true);
				return (cam, pt) -> {
					try {
						return (Frustum) method.invoke(null, cam, pt);
					} catch (Exception ignored) {
						return null;
					}
				};
			} catch (Exception ignored) {
				return ($, $$) -> null;
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

		@FunctionalInterface
		public interface FrustumConstructor {
			@Nullable
			Frustum create(Camera camera, float partialTicks);
		}
	}
}
