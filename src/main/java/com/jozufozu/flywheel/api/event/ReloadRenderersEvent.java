package com.jozufozu.flywheel.api.event;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.eventbus.api.Event;

public class ReloadRenderersEvent extends Event {
	private final ClientLevel level;

	public ReloadRenderersEvent(ClientLevel level) {
		this.level = level;
	}

	@Nullable
	public ClientLevel getLevel() {
		return level;
	}
}
