package com.jozufozu.flywheel.backend.compile;

import com.jozufozu.flywheel.api.Flywheel;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public final class FlwProgramsReloader implements SimpleSynchronousResourceReloadListener {
	public static final FlwProgramsReloader INSTANCE = new FlwProgramsReloader();

	public static final ResourceLocation ID = Flywheel.rl("programs");

	private FlwProgramsReloader() {
	}

	@Override
	public void onResourceManagerReload(ResourceManager manager) {
		FlwPrograms.reload(manager);
	}

	@Override
	public ResourceLocation getFabricId() {
		return ID;
	}
}
