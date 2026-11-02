package dev.engine_room.vanillin;

import dev.engine_room.vanillin.visuals.MinecartVisual;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public class VanillinClientResourceReloadListener implements ResourceManagerReloadListener {
	public static final Identifier ID = Vanillin.id("client_resource_reload_listener");
	public static final VanillinClientResourceReloadListener INSTANCE = new VanillinClientResourceReloadListener();

	private VanillinClientResourceReloadListener() {
	}

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		MinecartVisual.clearSpecialRendererCache();
	}
}
