package com.jozufozu.flywheel.backend;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.function.BooleanSupplier;

import org.apache.commons.lang3.mutable.MutableInt;

import net.minecraft.client.Minecraft;

public final class OptifineHandler {
	public static final String OPTIFINE_ROOT_PACKAGE = "net.optifine";
	public static final String SHADER_PACKAGE = "net.optifine.shaders";

	private static final ThreadLocal<MutableInt> FORCE_DIFFUSE = ThreadLocal.withInitial(MutableInt::new);

	private static boolean isOptifineInstalled;
	private static boolean isUsingShaders;
	private static BooleanSupplier shadowPassSupplier;

	private OptifineHandler() {
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

	private static boolean areShadersDisabledInOptifineConfigFile() {
		File dir = Minecraft.getInstance().gameDirectory;

		File shaderOptions = new File(dir, "optionsshaders.txt");

		boolean shadersOff = true;
		try (BufferedReader reader = new BufferedReader(new FileReader(shaderOptions))) {

			shadersOff = reader.lines()
					.anyMatch(it -> {
						String line = it.replaceAll("\\s", "");
						if (line.startsWith("shaderPack=")) {
							String setting = line.substring("shaderPack=".length());

							return setting.equals("OFF") || setting.equals("(internal)");
						}
						return false;
					});
		} catch (IOException e) {
			Backend.LOGGER.info("No shader config found.");
		}
		return shadersOff;
	}

	public static void init() {
		Package optifinePackage = Package.getPackage(OPTIFINE_ROOT_PACKAGE);
		isOptifineInstalled = optifinePackage != null;

		if (isOptifineInstalled) {
			Backend.LOGGER.info("Optifine detected.");

			refresh();
		} else {
			Backend.LOGGER.info("Optifine not detected.");
		}

		shadowPassSupplier = createShadowPassSupplier();
	}

	public static void refresh() {
		if (!isOptifineInstalled) return;

		boolean shadersOff = areShadersDisabledInOptifineConfigFile();

		isUsingShaders = !shadersOff;
	}

	public static boolean isOptifineInstalled() {
		return isOptifineInstalled;
	}

	public static boolean isUsingShaders() {
		return isUsingShaders;
	}

	public static boolean isShadowPass() {
		return shadowPassSupplier.getAsBoolean();
	}

	public static void pushForceDiffuse() {
		if (isOptifineInstalled) {
			FORCE_DIFFUSE.get().increment();
		}
	}

	public static void popForceDiffuse() {
		if (isOptifineInstalled) {
			FORCE_DIFFUSE.get().decrement();
		}
	}

	public static boolean shouldApplyDiffuse() {
		if (isOptifineInstalled) {
			return !isUsingShaders || (FORCE_DIFFUSE.get().intValue() > 0);
		}
		return true;
	}
}
