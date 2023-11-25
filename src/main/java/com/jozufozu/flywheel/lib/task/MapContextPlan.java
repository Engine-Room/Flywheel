package com.jozufozu.flywheel.lib.task;

import java.util.function.Function;

import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;

public record MapContextPlan<C, D>(Function<C, D> map, Plan<D> plan) implements SimplyComposedPlan<C> {
	public static <C, D> Builder<C, D> map(Function<C, D> map) {
		return new Builder<>(map);
	}

	@Override
	public void execute(TaskExecutor taskExecutor, C context, Runnable onCompletion) {
		D newContext = map.apply(context);
		plan.execute(taskExecutor, newContext, onCompletion);
	}

	@Override
	public Plan<C> simplify() {
		var maybeSimplified = plan.simplify();

		if (maybeSimplified instanceof UnitPlan) {
			return UnitPlan.of();
		}

		return new MapContextPlan<>(map, maybeSimplified);
	}

	public static class Builder<C, D> {
		private final Function<C, D> map;

		public Builder(Function<C, D> map) {
			this.map = map;
		}

		public MapContextPlan<C, D> to(Plan<D> plan) {
			return new MapContextPlan<>(map, plan);
		}

		public MapContextPlan<C, D> plan() {
			return new MapContextPlan<>(map, UnitPlan.of());
		}
	}
}
