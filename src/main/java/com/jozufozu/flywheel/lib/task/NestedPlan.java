package com.jozufozu.flywheel.lib.task;

import java.util.List;

import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;

public record NestedPlan(List<Plan> parallelPlans) implements Plan {
	@Override
	public void execute(TaskExecutor taskExecutor, Runnable onCompletion) {
		if (parallelPlans.isEmpty()) {
			onCompletion.run();
			return;
		}

		var size = parallelPlans.size();

		if (size == 1) {
			parallelPlans.get(0)
					.execute(taskExecutor, onCompletion);
			return;
		}

		var wait = new Synchronizer(size, onCompletion);
		for (Plan plan : parallelPlans) {
			plan.execute(taskExecutor, wait::decrementAndEventuallyRun);
		}
	}
}
