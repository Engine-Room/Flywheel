package com.jozufozu.flywheel.lib.task;

import java.util.function.Function;

import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;

public record MapContextPlan<C, D>(Function<C, D> map, Plan<D> plan) implements SimplyComposedPlan<C> {
	@Override
	public void execute(TaskExecutor taskExecutor, C context, Runnable onCompletion) {
		D newContext = map.apply(context);
		plan.execute(taskExecutor, newContext, onCompletion);
	}

	@Override
	public Plan<C> maybeSimplify() {
		var maybeSimplified = plan.maybeSimplify();

		if (maybeSimplified instanceof UnitPlan) {
			return UnitPlan.of();
		}

		return new MapContextPlan<>(map, maybeSimplified);
	}
}
