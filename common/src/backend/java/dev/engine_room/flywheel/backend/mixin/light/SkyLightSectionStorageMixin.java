package dev.engine_room.flywheel.backend.mixin.light;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import dev.engine_room.flywheel.backend.SkyLightSectionStorageExtension;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.lighting.LayerLightSectionStorage;
import net.minecraft.world.level.lighting.SkyLightSectionStorage;

@Mixin(SkyLightSectionStorage.class)
public abstract class SkyLightSectionStorageMixin extends LayerLightSectionStorage implements SkyLightSectionStorageExtension {
	protected SkyLightSectionStorageMixin() {
		super(null, null, null);
	}

	@Override
	@Nullable
	public DataLayer flywheel$skyDataLayer(long section) {
		// Logic copied from SkyLightSectionStorage#getLightValue, but here we directly return the DataLayer

		long l = section;
		int i = SectionPos.y(l);
		SkyDataLayerStorageMapAccessor skyDataLayerStorageMap = (SkyDataLayerStorageMapAccessor) this.visibleSectionData;
		int j = skyDataLayerStorageMap.flywheel$topSections()
				.get(SectionPos.getZeroNode(l));
		if (j != skyDataLayerStorageMap.flywheel$currentLowestY() && i < j) {
			DataLayer dataLayer = this.getDataLayerData(l);
			if (dataLayer == null) {
				for (; dataLayer == null; dataLayer = this.getDataLayerData(l)) {
					if (++i >= j) {
						return null;
					}

					l = SectionPos.offset(l, Direction.UP);
				}
			}

			return dataLayer;
		} else {
			return null;
		}
	}
}
