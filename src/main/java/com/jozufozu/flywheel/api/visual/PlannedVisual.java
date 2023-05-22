package com.jozufozu.flywheel.api.visual;

import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.lib.task.UnitPlan;

/**
 * An interface giving {@link Visual}s a way to define complex, parallelized update plans.
 * <p>
 * Plans allow for
 */
public interface PlannedVisual extends Visual {
	default Plan<VisualFrameContext> planFrame() {
		return UnitPlan.of();
	}

	default Plan<VisualTickContext> planTick() {
		return UnitPlan.of();
	}
}
