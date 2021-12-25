package com.jozufozu.flywheel.util;

import com.jozufozu.flywheel.mixin.PausedPartialTickAccessor;

import net.minecraft.client.Minecraft;

/**
 * Static access to tick-count and partialTick time, accounting for pausing.
 */
public class AnimationTickHolder {

	// Wrap around every 24 hours to maintain floating point accuracy.
	private static final int wrappingInterval = 1_728_000;
	private static int ticks;
	private static int paused_ticks;

	public static void tick() {
		if (!Minecraft.getInstance()
				.isPaused()) {
			ticks = (ticks + 1) % wrappingInterval;
		} else {
			paused_ticks = (paused_ticks + 1) % wrappingInterval;
		}
	}

	public static int getTicks() {
		return getTicks(false);
	}

	public static int getTicks(boolean includePaused) {
		return includePaused ? ticks + paused_ticks : ticks;
	}

	public static float getRenderTime() {
		return getTicks() + getPartialTicks();
	}

	public static float getPartialTicks() {
		Minecraft mc = Minecraft.getInstance();
		return (mc.isPaused() ? ((PausedPartialTickAccessor) mc).flywheel$getPartialTicksPaused() : mc.getFrameTime());
	}

	// Unused but might be useful for debugging.
	public static void _reset() {
		ticks = 0;
		paused_ticks = 0;
	}
}
