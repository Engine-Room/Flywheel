package dev.engine_room.flywheel.lib.task;

import dev.engine_room.flywheel.api.task.Plan;
import dev.engine_room.flywheel.api.task.TaskExecutor;

public record BarrierPlan<C>(Plan<C> first, Plan<C> second) implements SimplyComposedPlan<C> {
	public static <C> BarrierPlan<C> of(Plan<C> first, Plan<C> second) {
		return new BarrierPlan<>(first, second);
	}

	@Override
	public void execute(TaskExecutor taskExecutor, C context, Runnable onCompletion) {
		first.execute(taskExecutor, context, () -> second.execute(taskExecutor, context, onCompletion));
	}
}
