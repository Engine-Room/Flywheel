package dev.engine_room.flywheel.lib.task;

import dev.engine_room.flywheel.api.task.Plan;
import dev.engine_room.flywheel.api.task.TaskExecutor;
import dev.engine_room.flywheel.lib.task.functional.BooleanSupplierWithContext;

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

	public static final class Builder<C> {
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
