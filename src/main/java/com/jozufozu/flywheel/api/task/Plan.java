package com.jozufozu.flywheel.api.task;

import com.jozufozu.flywheel.lib.task.BarrierPlan;
import com.jozufozu.flywheel.lib.task.NestedPlan;

public interface Plan {
	/**
	 * Submit this plan for execution.
	 * <p>
	 * You <em>must</em> call {@code onCompletion.run()} when the plan has completed execution.
	 *
	 * @param taskExecutor The executor to use for submitting tasks.
	 * @param onCompletion A callback to run when the plan has completed execution, useful for chaining plans.
	 */
	void execute(TaskExecutor taskExecutor, Runnable onCompletion);

	default void execute(TaskExecutor taskExecutor) {
		execute(taskExecutor, () -> {
		});
	}

	/**
	 * Create a new plan that executes this plan, then the given plan.
	 *
	 * @param plan The plan to execute after this plan.
	 * @return The composed plan.
	 */
	default Plan then(Plan plan) {
		// TODO: AbstractPlan?
		return new BarrierPlan(this, plan);
	}

	/**
	 * Create a new plan that executes this plan and the given plan in parallel.
	 *
	 * @param plan The plan to execute in parallel with this plan.
	 * @return The composed plan.
	 */
	default Plan and(Plan plan) {
		return NestedPlan.of(this, plan);
	}

	/**
	 * If possible, create a new plan that accomplishes everything
	 * this plan does but with a simpler execution schedule.
	 *
	 * @return A simplified plan, or this.
	 */
	default Plan maybeSimplify() {
		// TODO: plan caching/simplification
		return this;
	}
}
