package dev.engine_room.flywheel.backend;

import dev.engine_room.flywheel.backend.compile.LightSmoothness;

public interface BackendConfig {
	BackendConfig INSTANCE = FlwBackendXplat.INSTANCE.getConfig();

	/**
	 * How smooth/accurate our flw_light impl is.
	 *
	 * <p>This makes more sense here as a backend-specific config because it's tightly coupled to
	 * our backend's implementation. 3rd party backend may have different approaches and configurations.
	 *
	 * @return The current light smoothness setting.
	 */
	LightSmoothness lightSmoothness();
}
