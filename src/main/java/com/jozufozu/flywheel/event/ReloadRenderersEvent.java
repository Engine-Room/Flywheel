package com.jozufozu.flywheel.event;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.fabric.event.EventContext;

import net.minecraft.client.multiplayer.ClientLevel;

public class ReloadRenderersEvent extends EventContext {
	private final ClientLevel world;

	public ReloadRenderersEvent(ClientLevel world) {
		this.world = world;
	}

	@Nullable
	public ClientLevel getWorld() {
		return world;
	}
}
