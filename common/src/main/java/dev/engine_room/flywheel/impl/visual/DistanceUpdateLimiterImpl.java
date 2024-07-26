package dev.engine_room.flywheel.impl.visual;

import dev.engine_room.flywheel.api.visual.DistanceUpdateLimiter;

public interface DistanceUpdateLimiterImpl extends DistanceUpdateLimiter {
	/**
	 * Call this before every update.
	 */
	void tick();
}
