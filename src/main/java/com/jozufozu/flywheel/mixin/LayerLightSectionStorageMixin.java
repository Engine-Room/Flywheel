package com.jozufozu.flywheel.mixin;

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
public abstract class LayerLightSectionStorageMixin {

	@Shadow
	@Final
	protected LongSet sectionsAffectedByLightUpdates;

	@Shadow
	@Final
	protected LightChunkGetter chunkSource;

	@Inject(at = @At("HEAD"), method = "swapSectionMap")
	private void flywheel$listenForChangedSections(CallbackInfo ci) {
		if (this.sectionsAffectedByLightUpdates.isEmpty()) {
			return;
		}

		var manager = VisualizationManagerImpl.get((LevelAccessor) this.chunkSource.getLevel());

		if (manager != null) {
			manager.getLightUpdater()
					.notifySectionUpdates(this.sectionsAffectedByLightUpdates);
		}
	}
}
