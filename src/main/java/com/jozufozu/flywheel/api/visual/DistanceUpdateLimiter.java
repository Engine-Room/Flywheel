package com.jozufozu.flywheel.api.visual;

/**
 * Interface for rate-limiting updates based on an object's distance from the camera.
 */
public interface DistanceUpdateLimiter {
	/**
	 * Check to see if an object at the given position relative to the camera should be updated.
	 *
	 * @param distanceSquared The distance squared from the camera to the object.
	 * @return {@code true} if the object should be updated, {@code false} otherwise.
	 */
	boolean shouldUpdate(double distanceSquared);
}
