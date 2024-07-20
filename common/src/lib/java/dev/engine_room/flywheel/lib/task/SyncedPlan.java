package dev.engine_room.flywheel.lib.task;

import dev.engine_room.flywheel.api.task.TaskExecutor;
import dev.engine_room.flywheel.lib.task.functional.RunnableWithContext;

public record SyncedPlan<C>(RunnableWithContext<C> task) implements SimplyComposedPlan<C> {
	public static <C> SyncedPlan<C> of(RunnableWithContext<C> task) {
		return new SyncedPlan<>(task);
	}

	public static <C> SyncedPlan<C> of(RunnableWithContext.Ignored<C> task) {
		return new SyncedPlan<>(task);
	}

	@Override
	public void execute(TaskExecutor taskExecutor, C context, Runnable onCompletion) {
		taskExecutor.scheduleForMainThread(() -> {
			task.run(context);
			onCompletion.run();
		});
	}
}
