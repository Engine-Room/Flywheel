package dev.engine_room.flywheel.impl.visual;

public class NonLimiter implements DistanceUpdateLimiterImpl {
	@Override
	public void tick() {
	}

	@Override
	public boolean shouldUpdate(double distanceSquared) {
		return true;
	}
}
