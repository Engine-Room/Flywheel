package dev.engine_room.flywheel.impl.task;

import java.util.function.BooleanSupplier;

import dev.engine_room.flywheel.api.task.TaskExecutor;

public interface TaskExecutorImpl extends TaskExecutor {
	/**
	 * Wait for <em>all</em> running tasks to finish.
	 * <br>
	 * This is useful as a nuclear option, but most of the time you should
	 * try to use {@link #syncUntil(BooleanSupplier) syncUntil}.
	 */
	void syncPoint();
}
