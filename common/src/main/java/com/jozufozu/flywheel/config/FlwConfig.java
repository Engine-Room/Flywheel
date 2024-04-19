package com.jozufozu.flywheel.config;

import com.jozufozu.flywheel.api.backend.Backend;
import com.jozufozu.flywheel.platform.ClientPlatform;

public interface FlwConfig {
	FlwConfig INSTANCE = ClientPlatform.getInstance().getConfigInstance();
	static FlwConfig get() {
		return INSTANCE;
	}

	Backend getBackend();

	boolean limitUpdates();

	int workerThreads();
}
