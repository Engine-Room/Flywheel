package com.jozufozu.flywheel.lib.task;

import java.util.Collection;

import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.jozufozu.flywheel.lib.task.functional.SupplierWithContext;

/**
 * A plan that executes many other plans provided dynamically.
 *
 * @param plans A function to get a collection of plans based on the context.
 * @param <C>   The type of the context object.
 */
public record DynamicNestedPlan<C>(
		SupplierWithContext<C, Collection<? extends Plan<C>>> plans) implements SimplyComposedPlan<C> {
	public static <C> Plan<C> of(SupplierWithContext.Ignored<C, Collection<? extends Plan<C>>> supplier) {
		return new DynamicNestedPlan<>(supplier);
	}

	public static <C> Plan<C> of(SupplierWithContext<C, Collection<? extends Plan<C>>> supplier) {
		return new DynamicNestedPlan<>(supplier);
	}

	@Override
	public void execute(TaskExecutor taskExecutor, C context, Runnable onCompletion) {
		var plans = this.plans.get(context);

		if (plans.isEmpty()) {
			onCompletion.run();
			return;
		}

		var sync = new Synchronizer(plans.size(), onCompletion);

		for (var plan : plans) {
			plan.execute(taskExecutor, context, sync);
		}
	}
}
