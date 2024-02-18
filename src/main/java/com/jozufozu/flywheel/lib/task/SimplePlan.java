package com.jozufozu.flywheel.lib.task;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.jozufozu.flywheel.lib.task.functional.RunnableWithContext;

public record SimplePlan<C>(List<RunnableWithContext<C>> parallelTasks) implements SimplyComposedPlan<C> {
	@SafeVarargs
	public static <C> SimplePlan<C> of(RunnableWithContext.Ignored<C>... tasks) {
		return new SimplePlan<>(List.of(tasks));
	}

	@SafeVarargs
	public static <C> SimplePlan<C> of(RunnableWithContext<C>... tasks) {
		return new SimplePlan<>(List.of(tasks));
	}

	public static <C> SimplePlan<C> of(List<RunnableWithContext<C>> tasks) {
		return new SimplePlan<>(tasks);
	}

	@Override
	public void execute(TaskExecutor taskExecutor, C context, Runnable onCompletion) {
		if (parallelTasks.isEmpty()) {
			onCompletion.run();
			return;
		}

		taskExecutor.execute(() -> Distribute.tasks(taskExecutor, context, onCompletion, parallelTasks, RunnableWithContext::run));
	}

	@Override
	public Plan<C> and(Plan<C> plan) {
		if (plan instanceof SimplePlan<C> simple) {
			return of(ImmutableList.<RunnableWithContext<C>>builder()
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
