package com.jozufozu.flywheel.lib.visual;

import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.Instancer;
import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.visual.DynamicVisual;
import com.jozufozu.flywheel.api.visual.TickableVisual;
import com.jozufozu.flywheel.lib.task.RunnablePlan;

public interface SimpleDynamicVisual extends DynamicVisual {
	/**
	 * Called every frame.
	 * <br>
	 * The implementation is free to parallelize calls to this method.
	 * You must ensure proper synchronization if you need to mutate anything outside this visual.
	 * <br>
	 * This method and {@link TickableVisual#tick} will never be called simultaneously.
	 * <br>
	 * {@link Instancer}/{@link Instance} creation/acquisition is safe here.
	 */
	void beginFrame(Context ctx);

	@Override
	default Plan<Context> planFrame() {
		return RunnablePlan.of(this::beginFrame);
	}
}
