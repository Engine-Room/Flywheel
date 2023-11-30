package com.jozufozu.flywheel.lib.task;

import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.jozufozu.flywheel.lib.task.functional.RunnableWithContext;

public record SyncedPlan<C>(RunnableWithContext<C> task) implements SimplyComposedPlan<C> {
	public static <C> Plan<C> of(RunnableWithContext<C> task) {
		return new SyncedPlan<>(task);
	}

	public static <C> Plan<C> of(RunnableWithContext.Ignored<C> task) {
		return new SyncedPlan<>(task);
	}

	@Override
	public void execute(TaskExecutor taskExecutor, C context, Runnable onCompletion) {
		if (taskExecutor.isMainThread()) {
			task.run(context);
			onCompletion.run();
			return;
		}
		taskExecutor.scheduleForMainThread(() -> {
			task.run(context);
			onCompletion.run();
		});
	}
}
