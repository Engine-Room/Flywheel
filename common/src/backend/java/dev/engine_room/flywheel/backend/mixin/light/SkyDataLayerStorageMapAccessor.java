package dev.engine_room.flywheel.backend.mixin.light;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;

@Mixin(targets = "net.minecraft.world.level.lighting.SkyLightSectionStorage$SkyDataLayerStorageMap")
public interface SkyDataLayerStorageMapAccessor {
	@Accessor("currentLowestY")
	int flywheel$currentLowestY();

	@Accessor("topSections")
	Long2IntOpenHashMap flywheel$topSections();
}
