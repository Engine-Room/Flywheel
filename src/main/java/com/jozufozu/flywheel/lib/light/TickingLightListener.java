package com.jozufozu.flywheel.lib.light;

// TODO: remove
public interface TickingLightListener extends LightListener {
	/**
	 * Called every tick for active listeners.
	 * @return {@code true} if the listener changed.
	 */
	boolean tickLightListener();
}
