package com.jozufozu.flywheel.event;

import com.jozufozu.flywheel.fabric.event.EventContext;

import javax.annotation.Nullable;

import net.minecraft.client.world.ClientWorld;

public class ReloadRenderersEvent extends EventContext {
	private final ClientWorld world;

	public ReloadRenderersEvent(ClientWorld world) {
		this.world = world;
	}

	@Nullable
	public ClientWorld getWorld() {
		return world;
	}
}
