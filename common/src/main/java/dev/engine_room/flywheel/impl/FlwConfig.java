package dev.engine_room.flywheel.impl;

import dev.engine_room.flywheel.api.backend.Backend;

public interface FlwConfig {
	FlwConfig INSTANCE = FlwImplXplat.INSTANCE.getConfig();

	Backend backend();

	boolean limitUpdates();

	int workerThreads();
}
