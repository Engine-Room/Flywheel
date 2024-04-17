package com.jozufozu.flywheel.api.visual;

import org.jetbrains.annotations.ApiStatus;

import com.jozufozu.flywheel.api.task.Plan;

/**
 * An interface giving {@link Visual}s a hook to have a function called at
 * the end of every tick.
 */
public interface TickableVisual extends Visual {
	Plan<Context> planTick();

	/**
	 * The context passed to the tick plan.
	 * <p>Currently this has no methods, it is reserved here for future use.</p>
	 */
	@ApiStatus.NonExtendable
	interface Context {
	}
}
