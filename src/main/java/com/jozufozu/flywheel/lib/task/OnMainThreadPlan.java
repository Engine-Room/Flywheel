package com.jozufozu.flywheel.lib.task;

import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;

public record OnMainThreadPlan(Runnable task) implements Plan {
	@Override
	public void execute(TaskExecutor taskExecutor, Runnable onCompletion) {
		// TODO: detect if we're already on the render thread and just run the task directly
		taskExecutor.scheduleForMainThread(() -> {
			task.run();
			onCompletion.run();
		});
	}
}
