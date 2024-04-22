package com.jozufozu.flywheel.config;

import com.jozufozu.flywheel.api.backend.Backend;
import com.jozufozu.flywheel.platform.ClientPlatform;

public interface FlwConfig {
	FlwConfig INSTANCE = ClientPlatform.INSTANCE.getConfigInstance();
	static FlwConfig get() {
		return INSTANCE;
	}

	Backend backend();

	boolean limitUpdates();

	int workerThreads();
}
