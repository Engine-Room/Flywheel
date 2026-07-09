package dev.engine_room.flywheel.backend.compile;

import dev.engine_room.flywheel.backend.NoiseTextures;
import dev.engine_room.flywheel.lib.util.ResourceUtil;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

public final class FlwProgramsReloader implements SimpleSynchronousResourceReloadListener {
	public static final FlwProgramsReloader INSTANCE = new FlwProgramsReloader();

	public static final Identifier ID = ResourceUtil.rl("programs");

	private FlwProgramsReloader() {
	}

	@Override
	public void onResourceManagerReload(ResourceManager manager) {
		FlwPrograms.reload(manager);
		NoiseTextures.reload(manager);
	}

	@Override
	public Identifier getFabricId() {
		return ID;
	}
}
