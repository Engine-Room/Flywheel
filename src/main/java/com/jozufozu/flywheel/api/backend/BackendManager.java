package com.jozufozu.flywheel.api.backend;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.impl.BackendManagerImpl;

public final class BackendManager {
	/**
	 * Get the current backend.
	 */
	@Nullable
	public static Backend getBackend() {
		return BackendManagerImpl.getBackend();
	}

	/**
	 * Get a string describing the current backend.
	 */
	public static String getBackendDescriptor() {
		return BackendManagerImpl.getBackendDescriptor();
	}

	public static boolean isOn() {
		return BackendManagerImpl.isOn();
	}

	// TODO: definitively sort existing calls to this method into API (include behavior in javadoc) or default backend code
	public static void refresh() {
		BackendManagerImpl.refresh();
	}

	public static Backend getDefaultBackend() {
		return BackendManagerImpl.getDefaultBackend();
	}

	private BackendManager() {
	}
}
