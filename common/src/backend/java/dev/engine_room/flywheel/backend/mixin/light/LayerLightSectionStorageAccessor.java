package dev.engine_room.flywheel.backend.mixin.light;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.lighting.LayerLightSectionStorage;

@Mixin(LayerLightSectionStorage.class)
public interface LayerLightSectionStorageAccessor {
	@Invoker("getDataLayer")
	@Nullable
	DataLayer flywheel$callGetDataLayer(long sectionPos, boolean cached);
}
