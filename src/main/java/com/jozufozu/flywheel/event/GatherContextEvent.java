package com.jozufozu.flywheel.event;

import com.jozufozu.flywheel.fabric.event.EventContext;

public class GatherContextEvent extends EventContext {

	private final boolean firstLoad;

	public GatherContextEvent(boolean firstLoad) {
		this.firstLoad = firstLoad;
	}

	/**
	 * @return true iff it is the first time the event is fired.
	 */
	public boolean isFirstLoad() {
		return firstLoad;
	}
}
