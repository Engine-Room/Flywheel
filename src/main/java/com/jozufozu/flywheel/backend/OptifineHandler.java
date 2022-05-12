package com.jozufozu.flywheel.backend;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.BooleanSupplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.culling.Frustum;

public final class OptifineHandler {
	public static final String OPTIFINE_ROOT_PACKAGE = "net.optifine";
	public static final String SHADER_PACKAGE = "net.optifine.shaders";

	private static boolean isOptifineInstalled;
	private static BooleanSupplier shadersEnabledSupplier;
	private static BooleanSupplier shadowPassSupplier;
	private static FrustumConstructor shadowFrustumConstructor;

	private OptifineHandler() {
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

	public static void init() {
		Package optifinePackage = Package.getPackage(OPTIFINE_ROOT_PACKAGE);
		isOptifineInstalled = optifinePackage != null;

		if (isOptifineInstalled) {
			Backend.LOGGER.info("Optifine detected.");
		} else {
			Backend.LOGGER.info("Optifine not detected.");
		}

		shadersEnabledSupplier = createShadersEnabledSupplier();
		shadowPassSupplier = createShadowPassSupplier();
		shadowFrustumConstructor = createShadowFrustumConstructor();
	}

	public static boolean isOptifineInstalled() {
		return isOptifineInstalled;
	}

	public static boolean isUsingShaders() {
		return shadersEnabledSupplier.getAsBoolean();
	}

	public static boolean isShadowPass() {
		return shadowPassSupplier.getAsBoolean();
	}

	@Nullable
	public static Frustum createShadowFrustum(Camera camera, float partialTicks) {
		var frustum = shadowFrustumConstructor.create(camera, partialTicks);
		if (frustum != null) {
			var position = camera.getPosition();
			frustum.prepare(position.x, position.y, position.z);
		}
		return frustum;
	}

	@FunctionalInterface
	public interface FrustumConstructor {
		@Nullable
		Frustum create(Camera camera, float partialTicks);
	}
}
