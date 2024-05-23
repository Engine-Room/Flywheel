package dev.engine_room.flywheel.api.event;

import java.util.Optional;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;

@FunctionalInterface
public interface EndClientResourceReloadCallback {
	Event<EndClientResourceReloadCallback> EVENT = EventFactory.createArrayBacked(EndClientResourceReloadCallback.class,
			callbacks -> (minecraft, resourceManager, initialReload, error) -> {
				for (EndClientResourceReloadCallback callback : callbacks) {
					callback.onEndClientResourceReload(minecraft, resourceManager, initialReload, error);
				}
			});

	void onEndClientResourceReload(Minecraft minecraft, ResourceManager resourceManager, boolean initialReload,
								   Optional<Throwable> error);
}
