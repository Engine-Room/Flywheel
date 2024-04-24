package com.jozufozu.flywheel.api.backend;

import com.jozufozu.flywheel.api.internal.FlwApiLink;

public final class BackendManager {
	private BackendManager() {
	}

	/**
	 * Get the current backend.
	 */
	public static Backend getBackend() {
		return FlwApiLink.INSTANCE.getBackend();
	}

	public static boolean isBackendOn() {
		return FlwApiLink.INSTANCE.isBackendOn();
	}

	public static Backend getOffBackend() {
		return FlwApiLink.INSTANCE.getOffBackend();
	}

	public static Backend getDefaultBackend() {
		return FlwApiLink.INSTANCE.getDefaultBackend();
	}
}
