package com.jozufozu.flywheel.impl;

import com.jozufozu.flywheel.api.backend.Backend;

public interface FlwConfig {
	FlwConfig INSTANCE = FlwImplXplat.INSTANCE.getConfig();

	Backend backend();

	boolean limitUpdates();

	int workerThreads();
}
