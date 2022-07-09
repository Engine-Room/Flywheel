package com.jozufozu.flywheel.light;

public interface TickingLightListener extends LightListener {
	/**
	 * Called every tick for active listeners.
	 * @return {@code true} if the listener changed.
	 */
	boolean tickLightListener();
}
