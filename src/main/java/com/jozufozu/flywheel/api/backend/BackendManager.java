package com.jozufozu.flywheel.api.backend;

import com.jozufozu.flywheel.api.internal.InternalFlywheelApi;

public final class BackendManager {
	private BackendManager() {
	}

	/**
	 * Get the current backend.
	 */
	public static Backend getBackend() {
		return InternalFlywheelApi.INSTANCE.getBackend();
	}

	public static boolean isBackendOn() {
		return InternalFlywheelApi.INSTANCE.isBackendOn();
	}

	public static Backend getOffBackend() {
		return InternalFlywheelApi.INSTANCE.getOffBackend();
	}

	public static Backend getDefaultBackend() {
		return InternalFlywheelApi.INSTANCE.getDefaultBackend();
	}
}
