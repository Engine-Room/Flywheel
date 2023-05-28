package com.jozufozu.flywheel.lib.task;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;

public record SimplePlan<C>(List<ContextConsumer<C>> parallelTasks) implements SimplyComposedPlan<C> {
	@SafeVarargs
	public static <C> SimplePlan<C> of(ContextRunnable<C>... tasks) {
		return new SimplePlan<>(List.of(tasks));
	}

	@SafeVarargs
	public static <C> SimplePlan<C> of(ContextConsumer<C>... tasks) {
		return new SimplePlan<>(List.of(tasks));
	}

	public static <C> SimplePlan<C> of(List<ContextConsumer<C>> tasks) {
		return new SimplePlan<>(tasks);
	}

	@Override
	public void execute(TaskExecutor taskExecutor, C context, Runnable onCompletion) {
		if (parallelTasks.isEmpty()) {
			onCompletion.run();
			return;
		}

		taskExecutor.execute(() -> {
			PlanUtil.distribute(taskExecutor, context, onCompletion, parallelTasks, ContextConsumer::accept);
		});
	}

	@Override
	public Plan<C> and(Plan<C> plan) {
		if (plan instanceof SimplePlan<C> simple) {
			return of(ImmutableList.<ContextConsumer<C>>builder()
					.addAll(parallelTasks)
					.addAll(simple.parallelTasks)
					.build());
		}
		return SimplyComposedPlan.super.and(plan);
	}

	@Override
	public Plan<C> simplify() {
		if (parallelTasks.isEmpty()) {
			return UnitPlan.of();
		}

		return this;
	}
}
