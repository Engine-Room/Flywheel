package com.jozufozu.flywheel.lib.task;

import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;

public record BarrierPlan(Plan first, Plan second) implements Plan {
	@Override
	public void execute(TaskExecutor taskExecutor, Runnable onCompletion) {
		first.execute(taskExecutor, () -> second.execute(taskExecutor, onCompletion));
	}

	@Override
	public Plan maybeSimplify() {
		var first = this.first.maybeSimplify();
		var second = this.second.maybeSimplify();

		if (first == UnitPlan.INSTANCE) {
			return second;
		}
		if (second == UnitPlan.INSTANCE) {
			return first;
		}

		return new BarrierPlan(first, second);
	}
}
