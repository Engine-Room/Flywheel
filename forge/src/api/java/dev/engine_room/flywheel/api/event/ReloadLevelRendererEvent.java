package dev.engine_room.flywheel.api.event;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.eventbus.api.Event;

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
