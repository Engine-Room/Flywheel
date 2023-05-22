package com.jozufozu.flywheel.lib.task;

import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;

public record OnMainThreadPlan<C>(ContextConsumer<C> task) implements SimplyComposedPlan<C> {
	public static <C> Plan<C> of(ContextConsumer<C> task) {
		return new OnMainThreadPlan<>(task);
	}

	public static <C> Plan<C> of(ContextRunnable<C> task) {
		return new OnMainThreadPlan<>(task);
	}

	@Override
	public void execute(TaskExecutor taskExecutor, C context, Runnable onCompletion) {
		taskExecutor.scheduleForMainThread(() -> {
			task.accept(context);
			onCompletion.run();
		});
	}
}
