package com.jozufozu.flywheel.api.visual;

import org.jetbrains.annotations.ApiStatus;

import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.Instancer;
import com.jozufozu.flywheel.api.task.Plan;

/**
 * An interface giving {@link Visual}s a hook to have a function called at
 * the end of every tick.
 */
public interface TickableVisual extends Visual {
	/**
	 * Invoked every tick.
	 * <br>
	 * The implementation is free to parallelize the invocation of this plan.
	 * You must ensure proper synchronization if you need to mutate anything outside this visual.
	 * <br>
	 * This plan and the one returned by {@link DynamicVisual#planFrame} will never be invoked simultaneously.
	 * <br>
	 * {@link Instancer}/{@link Instance} creation/acquisition is safe here.
	 */
	Plan<Context> planTick();

	/**
	 * The context passed to the tick plan.
	 * <p>Currently this has no methods, it is reserved here for future use.</p>
	 */
	@ApiStatus.NonExtendable
	interface Context {
	}
}
