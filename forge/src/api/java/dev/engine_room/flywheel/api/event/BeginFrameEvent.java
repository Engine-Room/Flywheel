package dev.engine_room.flywheel.api.event;

import net.minecraftforge.eventbus.api.Event;

/**
 * This event is posted to the Forge event bus.
 */
public final class BeginFrameEvent extends Event {
	private final RenderContext context;

	public BeginFrameEvent(RenderContext context) {
		this.context = context;
	}

	public RenderContext context() {
		return context;
	}
}
