package com.jozufozu.flywheel.lib.light;

import net.minecraft.world.level.LevelAccessor;

/**
 * Marker interface for custom/fake levels to indicate that LightUpdater should interact with it.<p>
 *
 * Implement this if your custom level has light updates at all. If so, be sure to call
 * {@link com.jozufozu.flywheel.lib.util.LevelAttached#invalidateLevel} when your level is unloaded.
 */
public interface LightUpdatedLevel extends LevelAccessor {
	/**
	 * @return {@code true} if this level is passing light updates into LightUpdater.
	 */
	default boolean receivesLightUpdates() {
		return true;
	}
}
