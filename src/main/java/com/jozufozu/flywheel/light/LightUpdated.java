package com.jozufozu.flywheel.light;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.LevelAccessor;

/**
 * Marker interface for custom/fake levels to indicate that LightUpdater should bother interacting with it.<p>
 *
 * Implement this if your custom level has light updates at all. If so, be sure to call
 * {@link com.jozufozu.flywheel.util.WorldAttached#invalidateWorld} when your level in unloaded.
 */
public interface LightUpdated extends LevelAccessor {

	/**
	 * @return {@code true} if this level is passing light updates into LightUpdater.
	 */
	default boolean receivesLightUpdates() {
		return true;
	}

	static boolean receivesLightUpdates(LevelAccessor level) {
		// The client level is guaranteed to receive updates.
		if (Minecraft.getInstance().level == level) {
			return true;
		}
		// Custom/fake levels need to indicate that LightUpdater has meaning.
		if (level instanceof LightUpdated c) {
			return c.receivesLightUpdates();
		}
		return false;
	}
}
