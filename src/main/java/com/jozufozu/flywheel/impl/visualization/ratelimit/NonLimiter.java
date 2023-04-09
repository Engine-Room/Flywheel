package com.jozufozu.flywheel.impl.visualization.ratelimit;

public class NonLimiter implements DistanceUpdateLimiter {
	@Override
	public void tick() {
	}

	@Override
	public boolean shouldUpdate(double distanceSquared) {
		return true;
	}
}
