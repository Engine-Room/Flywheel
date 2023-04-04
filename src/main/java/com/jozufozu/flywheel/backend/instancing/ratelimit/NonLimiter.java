package com.jozufozu.flywheel.backend.instancing.ratelimit;

public class NonLimiter implements DistanceUpdateLimiter {
	@Override
	public void tick() {
	}

	@Override
	public boolean shouldUpdate(int dX, int dY, int dZ) {
		return true;
	}
}
