package com.jozufozu.flywheel.lib.task;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;

public record NestedPlan(List<Plan> parallelPlans) implements Plan {
	public static NestedPlan of(Plan... plans) {
		return new NestedPlan(ImmutableList.copyOf(plans));
	}

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

	@Override
	public Plan and(Plan plan) {
		return new NestedPlan(ImmutableList.<Plan>builder()
				.addAll(parallelPlans)
				.add(plan)
				.build());
	}

	@Override
	public Plan maybeSimplify() {
		if (parallelPlans.isEmpty()) {
			return UnitPlan.INSTANCE;
		}

		if (parallelPlans.size() == 1) {
			return parallelPlans.get(0)
					.maybeSimplify();
		}

		var simplifiedTasks = new ArrayList<Runnable>();
		var simplifiedPlans = new ArrayList<Plan>();

		var toVisit = new ArrayDeque<>(parallelPlans);
		while (!toVisit.isEmpty()) {
			var plan = toVisit.pop()
					.maybeSimplify();

			if (plan == UnitPlan.INSTANCE) {
				continue;
			}

			if (plan instanceof SimplePlan simplePlan) {
				// merge all simple plans into one
				simplifiedTasks.addAll(simplePlan.parallelTasks());
			} else if (plan instanceof NestedPlan nestedPlan) {
				// inline and re-visit nested plans
				toVisit.addAll(nestedPlan.parallelPlans());
			} else {
				// /shrug
				simplifiedPlans.add(plan);
			}
		}

		if (simplifiedTasks.isEmpty() && simplifiedPlans.isEmpty()) {
			// everything got simplified away
			return UnitPlan.INSTANCE;
		}

		if (simplifiedTasks.isEmpty()) {
			// no simple plan to create
			if (simplifiedPlans.size() == 1) {
				// we only contained one complex plan, so we can just return that
				return simplifiedPlans.get(0);
			}
			return new NestedPlan(simplifiedPlans);
		}

		if (simplifiedPlans.isEmpty()) {
			// we only contained simple plans, so we can just return one
			return new SimplePlan(simplifiedTasks);
		}

		// we have both simple and complex plans, so we need to create a nested plan
		simplifiedPlans.add(new SimplePlan(simplifiedTasks));
		return new NestedPlan(simplifiedPlans);
	}
}
