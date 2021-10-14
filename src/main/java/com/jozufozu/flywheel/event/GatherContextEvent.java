package com.jozufozu.flywheel.event;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.fabric.event.EventContext;

public class GatherContextEvent extends EventContext {

	private final Backend backend;
	private final boolean firstLoad;

	public GatherContextEvent(Backend backend, boolean firstLoad) {
		this.backend = backend;
		this.firstLoad = firstLoad;
	}

	public Backend getBackend() {
		return backend;
	}

	/**
	 * @return true iff it is the first time the event is fired.
	 */
	public boolean isFirstLoad() {
		return firstLoad;
	}
}
