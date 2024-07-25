package dev.engine_room.flywheel.impl.task;

import dev.engine_room.flywheel.api.task.TaskExecutor;
import dev.engine_room.flywheel.lib.task.SimplyComposedPlan;

public record RaisePlan<C>(Flag flag) implements SimplyComposedPlan<C> {
	public static <C> RaisePlan<C> raise(Flag flag) {
		return new RaisePlan<>(flag);
	}

	@Override
	public void execute(TaskExecutor taskExecutor, C context, Runnable onCompletion) {
		flag.raise();
		onCompletion.run();
	}
}
