package com.jozufozu.flywheel.event;

import com.jozufozu.flywheel.fabric.event.EventContext;

import net.minecraft.client.multiplayer.ClientLevel;

import org.jetbrains.annotations.Nullable;

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
