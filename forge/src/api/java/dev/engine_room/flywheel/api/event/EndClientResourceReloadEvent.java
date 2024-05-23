package dev.engine_room.flywheel.api.event;

import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

/**
 * This event is posted to mod event buses.
 */
// TODO: This should not be a mod bus event. However, currently, it cannot be a Forge bus event since that bus is not started by the time this event needs to be posted.
public final class EndClientResourceReloadEvent extends Event implements IModBusEvent {
	private final Minecraft minecraft;
	private final ResourceManager resourceManager;
	private final boolean initialReload;
	private final Optional<Throwable> error;

	public EndClientResourceReloadEvent(Minecraft minecraft, ResourceManager resourceManager, boolean initialReload, Optional<Throwable> error) {
		this.minecraft = minecraft;
		this.resourceManager = resourceManager;
		this.initialReload = initialReload;
		this.error = error;
	}

	public Minecraft minecraft() {
		return minecraft;
	}

	public ResourceManager resourceManager() {
		return resourceManager;
	}

	public boolean isInitialReload() {
		return initialReload;
	}

	public Optional<Throwable> error() {
		return error;
	}
}
