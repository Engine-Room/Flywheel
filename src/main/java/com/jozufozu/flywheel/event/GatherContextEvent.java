package com.jozufozu.flywheel.event;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.fabric.event.EventContext;

public class GatherContextEvent extends EventContext {

	private final Backend backend;

	public GatherContextEvent(Backend backend) {
		this.backend = backend;
	}

	public Backend getBackend() {
		return backend;
	}
}
