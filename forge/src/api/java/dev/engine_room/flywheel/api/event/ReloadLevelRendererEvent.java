package dev.engine_room.flywheel.api.event;

import net.neoforged.bus.api.Event;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.multiplayer.ClientLevel;

/**
 * This event is posted to the Forge event bus.
 */
public final class ReloadLevelRendererEvent extends Event {
	@Nullable
	private final ClientLevel level;

	public ReloadLevelRendererEvent(@Nullable ClientLevel level) {
		this.level = level;
	}

	@Nullable
	public ClientLevel level() {
		return level;
	}
}
