package com.jozufozu.flywheel.event;

import com.jozufozu.flywheel.backend.Backend;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

public class GatherContextEvent extends Event implements IModBusEvent {

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
