package com.jozufozu.flywheel.backend.instancing.ratelimit;

public class NonLimiter implements DistanceUpdateLimiter {
	@Override
	public void tick() {
	}

	@Override
	public boolean shouldUpdate(double distanceSquared) {
		return true;
	}
}
