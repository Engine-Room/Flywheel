package com.jozufozu.flywheel.backend.instancing.ratelimit;

import net.minecraft.util.Mth;

public class BandedPrimeLimiter implements DistanceUpdateLimiter {
	// 1 followed by the prime numbers
	private static final int[] DIVISOR_SEQUENCE = new int[] { 1, 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31 };

	private int tickCount = 0;

	@Override
	public void tick() {
		tickCount++;
	}

	@Override
	public boolean shouldUpdate(double distanceSquared) {
		return (tickCount % getUpdateDivisor(distanceSquared)) == 0;
	}

	protected int getUpdateDivisor(double distanceSquared) {
		int dSq = Mth.ceil(distanceSquared);

		int i = (dSq / 2048);

		return DIVISOR_SEQUENCE[Mth.clamp(i, 0, DIVISOR_SEQUENCE.length - 1)];
	}
}
