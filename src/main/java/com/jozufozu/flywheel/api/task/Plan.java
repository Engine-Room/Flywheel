package com.jozufozu.flywheel.api.task;

import java.util.function.Function;

public interface Plan<C> {
	/**
	 * Submit this plan for execution.
	 * <p>
	 * You <em>must</em> call {@code onCompletion.run()} when the plan has completed execution.
	 *
	 * @param taskExecutor The executor to use for submitting tasks.
	 * @param context      An arbitrary context object that the plan wants to use at runtime.
	 * @param onCompletion A callback to run when the plan has completed execution, useful for chaining plans.
	 */
	void execute(TaskExecutor taskExecutor, C context, Runnable onCompletion);

	/**
	 * Submit this plan for execution when the caller does not care about the completion of this Plan.
	 *
	 * @param taskExecutor The executor to use for submitting tasks.
	 * @param context      An arbitrary context object that the plan wants to use at runtime.
	 */
	default void execute(TaskExecutor taskExecutor, C context) {
		execute(taskExecutor, context, () -> {
		});
	}

	/**
	 * Create a new plan that executes this plan, then the given plan.
	 *
	 * @param plan The plan to execute after this plan.
	 * @return The composed plan.
	 */
	Plan<C> then(Plan<C> plan);

	/**
	 * Create a new plan that executes this plan and the given plan in parallel.
	 *
	 * @param plan The plan to execute in parallel with this plan.
	 * @return The composed plan.
	 */
	Plan<C> and(Plan<C> plan);

	/**
	 * If possible, create a new plan that accomplishes everything
	 * this plan does but with a simpler execution schedule.
	 *
	 * @return A simplified plan, or this.
	 */
	Plan<C> simplify();
}
