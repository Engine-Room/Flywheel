package dev.engine_room.flywheel.backend.compile;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public final class FlwProgramsReloader implements ResourceManagerReloadListener {
	public static final FlwProgramsReloader INSTANCE = new FlwProgramsReloader();

	private FlwProgramsReloader() {
	}

	@Override
	public void onResourceManagerReload(ResourceManager manager) {
		FlwPrograms.reload(manager);
	}
}
