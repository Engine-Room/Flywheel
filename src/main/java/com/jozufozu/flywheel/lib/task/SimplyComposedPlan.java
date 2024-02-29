package com.jozufozu.flywheel.lib.task;

import com.jozufozu.flywheel.api.task.Plan;

public interface SimplyComposedPlan<C> extends Plan<C> {
	@Override
	default Plan<C> then(Plan<C> plan) {
		return new BarrierPlan<>(this, plan);
	}

	@Override
	default Plan<C> and(Plan<C> plan) {
		return NestedPlan.of(this, plan);
	}

}
