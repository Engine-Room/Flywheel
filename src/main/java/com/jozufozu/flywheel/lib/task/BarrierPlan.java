package com.jozufozu.flywheel.lib.task;

import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;

public record BarrierPlan<C>(Plan<C> first, Plan<C> second) implements SimplyComposedPlan<C> {
	public static <C> Plan<C> of(Plan<C> first, Plan<C> second) {
		return new BarrierPlan<>(first, second);
	}

	@Override
	public void execute(TaskExecutor taskExecutor, C context, Runnable onCompletion) {
		first.execute(taskExecutor, context, () -> second.execute(taskExecutor, context, onCompletion));
	}

	@Override
	public Plan<C> maybeSimplify() {
		var first = this.first.maybeSimplify();
		var second = this.second.maybeSimplify();

		if (first == UnitPlan.of()) {
			return second;
		}
		if (second == UnitPlan.of()) {
			return first;
		}

		return new BarrierPlan<>(first, second);
	}
}
