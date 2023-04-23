package com.jozufozu.flywheel.lib.task;

import java.util.List;

import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;

public record SimplePlan(List<Runnable> parallelTasks) implements ContextAgnosticPlan {
	public static <C> Plan<C> of(Runnable... tasks) {
		return new SimplePlan(List.of(tasks)).cast();
	}

	public static <C> Plan<C> of(List<Runnable> tasks) {
		return new SimplePlan(tasks).cast();
	}

	@Override
	public void execute(TaskExecutor taskExecutor, Runnable onCompletion) {
		if (parallelTasks.isEmpty()) {
			onCompletion.run();
			return;
		}

		var synchronizer = new Synchronizer(parallelTasks.size(), onCompletion);
		for (var task : parallelTasks) {
			taskExecutor.execute(() -> {
				task.run();
				synchronizer.decrementAndEventuallyRun();
			});
		}
	}

	@Override
	public Plan<Object> maybeSimplify() {
		if (parallelTasks.isEmpty()) {
			return UnitPlan.of();
		}

		return this;
	}
}
