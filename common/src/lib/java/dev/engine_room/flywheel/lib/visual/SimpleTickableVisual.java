package com.jozufozu.flywheel.lib.visual;

import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.Instancer;
import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.visual.TickableVisual;
import com.jozufozu.flywheel.lib.task.RunnablePlan;

public interface SimpleTickableVisual extends TickableVisual {

	/**
	 * Called every tick.
	 * <br>
	 * The implementation is free to parallelize calls to this method.
	 * You must ensure proper synchronization if you need to mutate anything outside this visual.
	 * <br>
	 * This method and {@link SimpleDynamicVisual#beginFrame} will never be called simultaneously.
	 * <br>
	 * {@link Instancer}/{@link Instance} creation/acquisition is safe here.
	 */
	void tick(Context context);

	@Override
	default Plan<Context> planTick() {
		return RunnablePlan.of(this::tick);
	}
}
