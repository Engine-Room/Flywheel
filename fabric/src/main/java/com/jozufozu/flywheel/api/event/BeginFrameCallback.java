package com.jozufozu.flywheel.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

@FunctionalInterface
public interface BeginFrameCallback {
	Event<BeginFrameCallback> EVENT = EventFactory.createArrayBacked(BeginFrameCallback.class, callbacks -> context -> {
		for (BeginFrameCallback callback : callbacks) {
			callback.onBeginFrame(context);
		}
	});

	void onBeginFrame(RenderContext context);
}
