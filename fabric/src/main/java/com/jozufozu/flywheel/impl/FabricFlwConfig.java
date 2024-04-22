package com.jozufozu.flywheel.impl;

import com.jozufozu.flywheel.api.backend.Backend;
import com.jozufozu.flywheel.backend.Backends;

// TODO: Fabric config
public class FabricFlwConfig implements FlwConfig {
	public static final FabricFlwConfig INSTANCE = new FabricFlwConfig();

	@Override
	public Backend backend() {
		return Backends.INDIRECT;
	}

	@Override
	public boolean limitUpdates() {
		return true;
	}

	@Override
	public int workerThreads() {
		return -1;
	}
}
