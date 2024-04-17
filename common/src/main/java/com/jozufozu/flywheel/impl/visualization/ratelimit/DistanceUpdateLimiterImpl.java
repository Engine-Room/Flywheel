package com.jozufozu.flywheel.impl.visualization.ratelimit;

import com.jozufozu.flywheel.api.visual.DistanceUpdateLimiter;

public interface DistanceUpdateLimiterImpl extends DistanceUpdateLimiter {
	/**
	 * Call this before every update.
	 */
	void tick();
}
