package com.jozufozu.flywheel.api.event;

import net.minecraftforge.eventbus.api.Event;

public class BeginFrameEvent extends Event {
	private final RenderContext context;

	public BeginFrameEvent(RenderContext context) {
		this.context = context;
	}

	public RenderContext getContext() {
		return context;
	}
}
