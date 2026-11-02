package dev.engine_room.flywheel.backend.compile;

import dev.engine_room.flywheel.backend.NoiseTextures;
import dev.engine_room.flywheel.lib.util.IdentifierUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public final class FlwBackendResourceReloader implements ResourceManagerReloadListener {
	public static final Identifier ID = IdentifierUtil.id("backend_resource_reloader");

	public static final FlwBackendResourceReloader INSTANCE = new FlwBackendResourceReloader();

	private FlwBackendResourceReloader() {
	}

	@Override
	public void onResourceManagerReload(ResourceManager manager) {
		FlwPrograms.reload(manager);
		NoiseTextures.reload(manager);
	}
}
