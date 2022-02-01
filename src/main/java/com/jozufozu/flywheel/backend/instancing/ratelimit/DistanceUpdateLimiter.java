package com.jozufozu.flywheel.backend.instancing.ratelimit;

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
	 * @param dX The X distance from the camera.
	 * @param dY The Y distance from the camera.
	 * @param dZ The Z distance from the camera.
	 * @return {@code true} if the object should be updated, {@code false} otherwise.
	 */
	boolean shouldUpdate(int dX, int dY, int dZ);
}
