package dev.engine_room.flywheel.backend;

import org.jetbrains.annotations.UnknownNullability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.engine_room.flywheel.api.Flywheel;

public final class FlwBackend {
	public static final Logger LOGGER = LoggerFactory.getLogger(Flywheel.ID + "/backend");
	@UnknownNullability
	private static BackendConfig config;

	private FlwBackend() {
	}

	public static BackendConfig config() {
		return config;
	}

	public static void init(BackendConfig config) {
		FlwBackend.config = config;
		Backends.init();
	}
}
