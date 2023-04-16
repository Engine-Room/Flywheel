package com.jozufozu.flywheel.lib.util;

import com.jozufozu.flywheel.mixin.PausedPartialTickAccessor;

import net.minecraft.client.Minecraft;

/**
 * Static access to tick-count and partialTick time, accounting for pausing.
 */
public final class AnimationTickHolder {
	// Wrap around every 24 hours to maintain floating point accuracy.
	private static final int WRAPPING_INTERVAL = 1_728_000;
	private static int ticks;
	private static int pausedTicks;

	public static void tick() {
		if (!Minecraft.getInstance()
				.isPaused()) {
			ticks = (ticks + 1) % WRAPPING_INTERVAL;
		} else {
			pausedTicks = (pausedTicks + 1) % WRAPPING_INTERVAL;
		}
	}

	public static int getTicks() {
		return getTicks(false);
	}

	public static int getTicks(boolean includePaused) {
		return includePaused ? ticks + pausedTicks : ticks;
	}

	public static float getRenderTime() {
		return getTicks() + getPartialTicks();
	}

	public static float getPartialTicks() {
		Minecraft mc = Minecraft.getInstance();
		return (mc.isPaused() ? ((PausedPartialTickAccessor) mc).flywheel$getPausePartialTick() : mc.getFrameTime());
	}

	// Unused but might be useful for debugging.
	public static void _reset() {
		ticks = 0;
		pausedTicks = 0;
	}
}
