package dev.engine_room.flywheel.backend.compile;

import dev.engine_room.flywheel.backend.NoiseTextures;
import dev.engine_room.flywheel.lib.util.IdentifierUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public final class FlwProgramsReloader implements ResourceManagerReloadListener {
	public static final Identifier ID = IdentifierUtil.id("programs");

	public static final FlwProgramsReloader INSTANCE = new FlwProgramsReloader();

	private FlwProgramsReloader() {
	}

	@Override
	public void onResourceManagerReload(ResourceManager manager) {
		FlwPrograms.reload(manager);
		NoiseTextures.reload(manager);
	}
}
