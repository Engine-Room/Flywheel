package com.jozufozu.flywheel.lib.task;

import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;

public record SyncedPlan<C>(ContextConsumer<C> task) implements SimplyComposedPlan<C> {
	public static <C> Plan<C> of(ContextConsumer<C> task) {
		return new SyncedPlan<>(task);
	}

	public static <C> Plan<C> of(ContextRunnable<C> task) {
		return new SyncedPlan<>(task);
	}

	@Override
	public void execute(TaskExecutor taskExecutor, C context, Runnable onCompletion) {
		taskExecutor.scheduleForSync(() -> {
			task.accept(context);
			onCompletion.run();
		});
	}
}
