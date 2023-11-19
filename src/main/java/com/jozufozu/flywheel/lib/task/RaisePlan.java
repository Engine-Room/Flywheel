package com.jozufozu.flywheel.lib.task;

import com.jozufozu.flywheel.api.task.Flag;
import com.jozufozu.flywheel.api.task.TaskExecutor;

public record RaisePlan<C>(Flag flag) implements SimplyComposedPlan<C> {
	public static <C> RaisePlan<C> raise(Flag flag) {
		return new RaisePlan<>(flag);
	}

	@Override
	public void execute(TaskExecutor taskExecutor, C context, Runnable onCompletion) {
		taskExecutor.raise(flag);
		onCompletion.run();
	}
}
