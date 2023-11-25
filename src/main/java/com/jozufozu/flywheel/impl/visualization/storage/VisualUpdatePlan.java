package com.jozufozu.flywheel.impl.visualization.storage;

import java.util.List;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.jozufozu.flywheel.lib.task.NestedPlan;
import com.jozufozu.flywheel.lib.task.SimplyComposedPlan;
import com.jozufozu.flywheel.lib.task.UnitPlan;

public class VisualUpdatePlan<C> implements SimplyComposedPlan<C> {
	private final Supplier<List<Plan<C>>> initializer;
	@NotNull
	private Plan<C> plan = UnitPlan.of();
	private boolean initialized = false;
	private boolean needsSimplify = true;

	public VisualUpdatePlan(Supplier<List<Plan<C>>> initializer) {
		this.initializer = initializer;
	}

	@Override
	public void execute(TaskExecutor taskExecutor, C context, Runnable onCompletion) {
		updatePlans().execute(taskExecutor, context, onCompletion);
	}

	public void add(Plan<C> plan) {
		this.plan = this.plan.and(plan);

		needsSimplify = true;
	}

	private Plan<C> updatePlans() {
		if (!initialized) {
			Plan<C> mainPlan = new NestedPlan<>(initializer.get());
			plan = mainPlan.and(plan);
			plan = plan.simplify();
			initialized = true;
		} else if (needsSimplify) {
			plan = plan.simplify();
		}

		needsSimplify = false;
		return plan;
	}

	public void clear() {
		plan = UnitPlan.of();
		initialized = false;
	}
}
