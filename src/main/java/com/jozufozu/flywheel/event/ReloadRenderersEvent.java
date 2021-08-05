package com.jozufozu.flywheel.event;

import javax.annotation.Nullable;

import net.minecraft.client.world.ClientWorld;
import net.minecraftforge.eventbus.api.Event;

public class ReloadRenderersEvent extends Event {
	private final ClientWorld world;

	public ReloadRenderersEvent(ClientWorld world) {
		this.world = world;
	}

	@Nullable
	public ClientWorld getWorld() {
		return world;
	}
}
