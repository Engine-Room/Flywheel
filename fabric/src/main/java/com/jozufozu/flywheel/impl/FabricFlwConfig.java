package com.jozufozu.flywheel.impl;

import com.jozufozu.flywheel.api.backend.Backend;
import com.jozufozu.flywheel.api.backend.BackendManager;

// TODO: Fabric config
public class FabricFlwConfig implements FlwConfig {
	public static final FabricFlwConfig INSTANCE = new FabricFlwConfig();

	public Backend backend = BackendManager.getDefaultBackend();
	public boolean limitUpdates = true;
	public int workerThreads = -1;

	@Override
	public Backend backend() {
		return backend;
	}

	@Override
	public boolean limitUpdates() {
		return limitUpdates;
	}

	@Override
	public int workerThreads() {
		return workerThreads;
	}
}
