package com.jozufozu.flywheel.api.backend;

import com.jozufozu.flywheel.impl.BackendManagerImpl;

public final class BackendManager {
	/**
	 * Get the current backend.
	 */
	public static Backend getBackend() {
		return BackendManagerImpl.getBackend();
	}

	public static boolean isOn() {
		return BackendManagerImpl.isOn();
	}

	public static Backend getOffBackend() {
		return BackendManagerImpl.getOffBackend();
	}

	public static Backend getDefaultBackend() {
		return BackendManagerImpl.getDefaultBackend();
	}

	private BackendManager() {
	}
}
