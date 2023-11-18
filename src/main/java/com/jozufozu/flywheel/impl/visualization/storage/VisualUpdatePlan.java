package com.jozufozu.flywheel.impl.visualization.storage;

import java.util.List;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.jozufozu.flywheel.lib.task.NestedPlan;
import com.jozufozu.flywheel.lib.task.SimplyComposedPlan;

public class VisualUpdatePlan<C> implements SimplyComposedPlan<C> {
	private final Supplier<List<Plan<C>>> initializer;
	@Nullable
	private Plan<C> plan;
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
		if (this.plan == null) {
			this.plan = plan;
		} else {
			this.plan = this.plan.and(plan);
		}

		needsSimplify = true;
	}

	@NotNull
	private Plan<C> updatePlans() {
		if (!initialized) {
			Plan<C> mainPlan = new NestedPlan<>(initializer.get());
			if (plan != null) {
				plan = mainPlan.and(plan);
			} else {
				plan = mainPlan;
			}
			plan = plan.simplify();
			initialized = true;
		} else if (needsSimplify) {
			plan = plan.simplify();
		}

		needsSimplify = false;
		return plan;
	}

	public void clear() {
		plan = null;
		initialized = false;
	}
}
