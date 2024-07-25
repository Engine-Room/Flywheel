package dev.engine_room.flywheel.api.task;

import java.util.concurrent.Executor;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface TaskExecutor extends Executor {
	/**
	 * Check for the number of threads this executor uses.
	 * <br>
	 * May be helpful when determining how many chunks to divide a task into.
	 *
	 * @return The number of threads this executor uses.
	 */
	int threadCount();
}
