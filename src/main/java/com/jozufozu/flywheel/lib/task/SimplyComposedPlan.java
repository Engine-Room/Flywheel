package com.jozufozu.flywheel.lib.task;

import java.util.function.Function;

import com.jozufozu.flywheel.api.task.Plan;

public interface SimplyComposedPlan<C> extends Plan<C> {
	@Override
	default Plan<C> then(Plan<C> plan) {
		return new BarrierPlan<>(this, plan);
	}

	@Override
	default <D> Plan<C> thenMap(Function<C, D> map, Plan<D> plan) {
		return then(new MapContextPlan<>(map, plan));
	}

	@Override
	default Plan<C> and(Plan<C> plan) {
		return NestedPlan.of(this, plan);
	}

	@Override
	default <D> Plan<C> andMap(Function<C, D> map, Plan<D> plan) {
		return and(new MapContextPlan<>(map, plan));
	}

	@Override
	default Plan<C> simplify() {
		return this;
	}
}
