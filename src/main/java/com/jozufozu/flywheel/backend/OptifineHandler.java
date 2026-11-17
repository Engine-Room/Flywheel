package com.jozufozu.flywheel.backend;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.Optional;

import net.minecraft.client.Minecraft;

public class OptifineHandler {
	public static final String OPTIFINE_ROOT_PACKAGE = "net.optifine";
	public static final String SHADER_PACKAGE = "net.optifine.shaders";
	private static final String IRIS_API_CLASS = "net.irisshaders.iris.api.v0.IrisApi";

	private static Package optifine;
	private static boolean irisApiDetected;
	private static Method irisApiGetInstance;
	private static Method irisApiIsShaderPackInUse;
	private static boolean irisApiErrorLogged;
	private static OptifineHandler handler;

	public final boolean usingShaders;

	public OptifineHandler(boolean usingShaders) {
		this.usingShaders = usingShaders;
	}

	/**
	 * Get information about the current Optifine configuration.
	 *
	 * @return {@link Optional#empty()} if Optifine is not installed.
	 */
	public static Optional<OptifineHandler> get() {
		return Optional.ofNullable(handler);
	}

	public static boolean optifineInstalled() {
		return optifine != null;
	}

	public static boolean usingShaders() {
		if (irisApiDetected) {
			return irisUsingShaders();
		}

		return OptifineHandler.get()
				.map(OptifineHandler::isUsingShaders)
				.orElse(false);
	}

	/**
	 * Query the public Iris API reflectively so Oculus remains an optional dependency.
	 * Oculus is an Iris fork and does not use OptiFine's net.optifine package.
	 */
	private static boolean irisUsingShaders() {
		// Fail closed if an Iris API is present but cannot be queried. In that case,
		// do not let Flywheel reset the shader pipeline's active GL program.
		if (irisApiGetInstance == null || irisApiIsShaderPackInUse == null) return true;

		try {
			Object irisApi = irisApiGetInstance.invoke(null);
			if (irisApi == null) return true;
			return Boolean.TRUE.equals(irisApiIsShaderPackInUse.invoke(irisApi));
		} catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
			if (!irisApiErrorLogged) {
				Backend.log.warn("Could not query the Oculus/Iris shader state; treating shaders as active to protect the GL state.", e);
				irisApiErrorLogged = true;
			}
			return true;
		}
	}

	private static void detectIrisApi() {
		try {
			Class<?> irisApi = Class.forName(IRIS_API_CLASS, false, OptifineHandler.class.getClassLoader());
			irisApiDetected = true;
			irisApiGetInstance = irisApi.getMethod("getInstance");
			irisApiIsShaderPackInUse = irisApi.getMethod("isShaderPackInUse");
			Backend.log.info("Iris/Oculus shader API detected.");
		} catch (ClassNotFoundException e) {
			irisApiDetected = false;
		} catch (NoSuchMethodException e) {
			// An incompatible Iris API is safer to treat as active than to reset its GL program.
			irisApiDetected = true;
			Backend.log.warn("Iris/Oculus was detected, but its shader-state API was not found. Flywheel rendering will be disabled while it is present.", e);
		} catch (LinkageError e) {
			irisApiDetected = true;
			Backend.log.warn("Iris/Oculus API could not be linked. Treating shaders as active to protect the GL state.", e);
		}
	}

	public static void init() {
		detectIrisApi();
		optifine = Package.getPackage(OPTIFINE_ROOT_PACKAGE);

		if (optifine == null) {
			Backend.log.info("Optifine not detected.");
		} else {
			Backend.log.info("Optifine detected.");

			refresh();
		}
	}

	public static void refresh() {
		if (optifine == null) return;

		File dir = Minecraft.getInstance().gameDirectory;

		File shaderOptions = new File(dir, "optionsshaders.txt");

		boolean shadersOff = true;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(shaderOptions));

			shadersOff = reader.lines()
					.anyMatch(it -> {
						String line = it.replaceAll("\\s", "");
						if (line.startsWith("shaderPack=")) {
							String setting = line.substring("shaderPack=".length());

							return setting.equals("OFF") || setting.equals("(internal)");
						}
						return false;
					});
		} catch (FileNotFoundException e) {
			Backend.log.info("No shader config found.");
		}

		handler = new OptifineHandler(!shadersOff);
	}

	public boolean isUsingShaders() {
		return usingShaders;
	}
}
