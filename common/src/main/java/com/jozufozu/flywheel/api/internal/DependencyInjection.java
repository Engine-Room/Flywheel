package com.jozufozu.flywheel.api.internal;

import java.lang.reflect.Constructor;

// Adapted from https://github.com/CaffeineMC/sodium-fabric/blob/bf4fc9dab16e1cca07b2f23a1201c9bf237c8044/src/api/java/net/caffeinemc/mods/sodium/api/internal/DependencyInjection.java
public final class DependencyInjection {
	private DependencyInjection() {
	}

	public static <T> T load(Class<T> apiClass, String implClassName) {
		Class<?> implClass;

		try {
			implClass = Class.forName(implClassName);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Could not find implementation", e);
		}

		if (!apiClass.isAssignableFrom(implClass)) {
			throw new RuntimeException("Class %s does not implement interface %s"
					.formatted(implClass.getName(), apiClass.getName()));
		}

		Constructor<?> implConstructor;

		try {
			implConstructor = implClass.getConstructor();
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Could not find default constructor", e);
		}

		Object implInstance;

		try {
			implInstance = implConstructor.newInstance();
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Could not instantiate implementation", e);
		}

		return apiClass.cast(implInstance);
	}
}
