package com.jozufozu.flywheel.lib.task;

import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.jozufozu.flywheel.lib.task.functional.BooleanSupplierWithContext;

/**
 * Executes one plan or another, depending on a dynamically evaluated condition.
 * @param condition The condition to branch on.
 * @param onTrue The plan to execute if the condition is true.
 * @param onFalse The plan to execute if the condition is false.
 * @param <C> The type of the context object.
 */
public record IfElsePlan<C>(BooleanSupplierWithContext<C> condition, Plan<C> onTrue,
							Plan<C> onFalse) implements SimplyComposedPlan<C> {
	public static <C> Builder<C> on(BooleanSupplierWithContext<C> condition) {
		return new Builder<>(condition);
	}

	public static <C> Builder<C> on(BooleanSupplierWithContext.Ignored<C> condition) {
		return new Builder<>(condition);
	}

	@Override
	public void execute(TaskExecutor taskExecutor, C context, Runnable onCompletion) {
		if (condition.getAsBoolean(context)) {
			onTrue.execute(taskExecutor, context, onCompletion);
		} else {
			onFalse.execute(taskExecutor, context, onCompletion);
		}
	}

	@Override
	public Plan<C> simplify() {
		var maybeSimplifiedTrue = onTrue.simplify();
		var maybeSimplifiedFalse = onFalse.simplify();

		if (maybeSimplifiedTrue instanceof UnitPlan && maybeSimplifiedFalse instanceof UnitPlan) {
			// The condition may have side effects that still need to be evaluated.
			return SimplePlan.of(condition::test);
		}

		return new IfElsePlan<>(condition, maybeSimplifiedTrue, maybeSimplifiedFalse);
	}

	public static class Builder<C> {
		private final BooleanSupplierWithContext<C> condition;
		private Plan<C> onTrue = UnitPlan.of();
		private Plan<C> onFalse = UnitPlan.of();

		public Builder(BooleanSupplierWithContext<C> condition) {
			this.condition = condition;
		}

		public Builder<C> ifTrue(Plan<C> onTrue) {
			this.onTrue = onTrue;
			return this;
		}

		public Builder<C> ifFalse(Plan<C> onFalse) {
			this.onFalse = onFalse;
			return this;
		}

		public IfElsePlan<C> plan() {
			return new IfElsePlan<>(condition, onTrue, onFalse);
		}
	}
}
