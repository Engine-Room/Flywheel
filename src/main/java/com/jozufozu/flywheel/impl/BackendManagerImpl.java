package com.jozufozu.flywheel.impl;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.jozufozu.flywheel.api.backend.Backend;
import com.jozufozu.flywheel.config.FlwConfig;
import com.jozufozu.flywheel.lib.backend.Backends;
import com.mojang.logging.LogUtils;

public final class BackendManagerImpl {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Backend DEFAULT_BACKEND = findDefaultBackend();
	private static Backend backend;

	@Nullable
	public static Backend getBackend() {
		return backend;
	}

	public static String getBackendNameForCrashReport() {
		if (backend == null) {
			return "Uninitialized";
		}
		var backendId = Backend.REGISTRY.getId(backend);
		if (backendId == null) {
			return "Unregistered";
		}
		return backendId.toString();
	}

	public static boolean isOn() {
		return backend != null && backend != Backends.OFF;
	}

	public static void refresh() {
		backend = chooseBackend();
	}

	public static Backend getDefaultBackend() {
		return DEFAULT_BACKEND;
	}

	private static Backend chooseBackend() {
		var preferred = FlwConfig.get().getBackend();
		var actual = preferred.findFallback();

		if (preferred != actual) {
			LOGGER.warn("Flywheel backend fell back from '{}' to '{}'", Backend.REGISTRY.getIdOrThrow(preferred), Backend.REGISTRY.getIdOrThrow(actual));
		}

		return actual;
	}

	private static Backend findDefaultBackend() {
		// TODO: Automatically select the best default config based on the user's driver
		// TODO: Figure out how this will work if custom backends are registered
		return Backends.INDIRECT;
	}

	private BackendManagerImpl() {
	}
}
