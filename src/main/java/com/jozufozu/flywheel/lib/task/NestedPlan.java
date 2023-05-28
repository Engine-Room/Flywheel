package com.jozufozu.flywheel.lib.task;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;

public record NestedPlan<C>(List<Plan<C>> parallelPlans) implements SimplyComposedPlan<C> {
	@SafeVarargs
	public static <C> NestedPlan<C> of(Plan<C>... plans) {
		return new NestedPlan<>(ImmutableList.copyOf(plans));
	}

	@Override
	public void execute(TaskExecutor taskExecutor, C context, Runnable onCompletion) {
		if (parallelPlans.isEmpty()) {
			onCompletion.run();
			return;
		}

		var size = parallelPlans.size();

		if (size == 1) {
			parallelPlans.get(0)
					.execute(taskExecutor, context, onCompletion);
			return;
		}

		var wait = new Synchronizer(size, onCompletion);
		for (var plan : parallelPlans) {
			plan.execute(taskExecutor, context, wait);
		}
	}

	@Override
	public Plan<C> and(Plan<C> plan) {
		return new NestedPlan<>(ImmutableList.<Plan<C>>builder()
				.addAll(parallelPlans)
				.add(plan)
				.build());
	}

	@Override
	public Plan<C> simplify() {
		if (parallelPlans.isEmpty()) {
			return UnitPlan.of();
		}

		if (parallelPlans.size() == 1) {
			return parallelPlans.get(0)
					.simplify();
		}

		var simplifiedTasks = new ArrayList<ContextConsumer<C>>();
		var simplifiedPlans = new ArrayList<Plan<C>>();
		var toVisit = new ArrayDeque<>(parallelPlans);
		while (!toVisit.isEmpty()) {
			var plan = toVisit.pop()
					.simplify();

			if (plan == UnitPlan.of()) {
				continue;
			}

			if (plan instanceof SimplePlan<C> simplePlan) {
				// merge all simple plans into one
				simplifiedTasks.addAll(simplePlan.parallelTasks());
			} else if (plan instanceof NestedPlan<C> nestedPlan) {
				// inline and re-visit nested plans
				toVisit.addAll(nestedPlan.parallelPlans());
			} else {
				// /shrug
				simplifiedPlans.add(plan);
			}
		}

		if (simplifiedTasks.isEmpty() && simplifiedPlans.isEmpty()) {
			// everything got simplified away
			return UnitPlan.of();
		}

		if (simplifiedTasks.isEmpty()) {
			// no simple plan to create
			if (simplifiedPlans.size() == 1) {
				// we only contained one complex plan, so we can just return that
				return simplifiedPlans.get(0);
			}
			return new NestedPlan<>(simplifiedPlans);
		}

		if (simplifiedPlans.isEmpty()) {
			// we only contained simple plans, so we can just return one
			return SimplePlan.of(simplifiedTasks);
		}

		// we have both simple and complex plans, so we need to create a nested plan
		simplifiedPlans.add(SimplePlan.of(simplifiedTasks));
		return new NestedPlan<>(simplifiedPlans);
	}
}
