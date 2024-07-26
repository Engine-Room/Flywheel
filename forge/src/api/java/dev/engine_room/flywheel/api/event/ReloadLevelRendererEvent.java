package dev.engine_room.flywheel.api.event;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.eventbus.api.Event;

/**
 * This event is posted to the Forge event bus.
 */
public final class ReloadLevelRendererEvent extends Event {
	private final ClientLevel level;

	public ReloadLevelRendererEvent(ClientLevel level) {
		this.level = level;
	}

	public ClientLevel level() {
		return level;
	}
}
