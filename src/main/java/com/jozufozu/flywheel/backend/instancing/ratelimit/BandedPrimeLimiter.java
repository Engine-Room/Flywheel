package com.jozufozu.flywheel.backend.instancing.ratelimit;

import net.minecraft.util.Mth;

public class BandedPrimeLimiter implements DistanceUpdateLimiter {
	// 1 followed by the prime numbers
	private static final int[] divisorSequence = new int[] { 1, 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31 };

	private int tickCount = 0;

	@Override
	public void tick() {
		tickCount++;
	}

	@Override
	public boolean shouldUpdate(int dX, int dY, int dZ) {
		return (tickCount % getUpdateDivisor(dX, dY, dZ)) == 0;
	}

	protected int getUpdateDivisor(int dX, int dY, int dZ) {
		int dSq = dX * dX + dY * dY + dZ * dZ;

		int i = (dSq / 2048);

		return divisorSequence[Mth.clamp(i, 0, divisorSequence.length - 1)];
	}
}
