package com.jozufozu.flywheel.impl.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.impl.visualization.VisualizationManagerImpl;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LayerLightSectionStorage;

@Mixin(LayerLightSectionStorage.class)
abstract class LayerLightSectionStorageMixin {
	@Shadow
	@Final
	protected LightChunkGetter chunkSource;
	@Shadow
	@Final
	protected LongSet sectionsAffectedByLightUpdates;

	@Inject(method = "swapSectionMap()V", at = @At("HEAD"))
	private void flywheel$listenForChangedSections(CallbackInfo ci) {
		if (sectionsAffectedByLightUpdates.isEmpty()) {
			return;
		}

		if (!(chunkSource.getLevel() instanceof LevelAccessor level)) {
			return;
		}

		var manager = VisualizationManagerImpl.get(level);

		if (manager != null) {
			manager.enqueueLightUpdateSections(sectionsAffectedByLightUpdates);
		}
	}
}
