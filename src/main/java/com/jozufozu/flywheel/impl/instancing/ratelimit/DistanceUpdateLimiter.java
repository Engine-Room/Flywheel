package com.jozufozu.flywheel.impl.instancing.ratelimit;

/**
 * Interface for rate-limiting updates based on an object's distance from the camera.
 */
public interface DistanceUpdateLimiter {
	/**
	 * Call this before every update.
	 */
	void tick();

	/**
	 * Check to see if an object at the given position relative to the camera should be updated.
	 *
	 * @param distanceSquared
	 * @return {@code true} if the object should be updated, {@code false} otherwise.
	 */
	boolean shouldUpdate(double distanceSquared);
}
