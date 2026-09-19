package dev.engine_room.flywheel.backend.extension;

import org.jspecify.annotations.Nullable;

import net.minecraft.world.level.chunk.DataLayer;

public interface SkyLightSectionStorageExtension {
	@Nullable DataLayer flywheel$skyDataLayer(long section);
}
