package dev.engine_room.flywheel.lib.task;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A flag that can be raised and lowered in a thread-safe fashion.
 * <br>
 * Useful when combined with {@link RaisePlan} and {@link dev.engine_room.flywheel.api.task.TaskExecutor#syncUntil TaskExecutor.syncUntil}.
 */
public class Flag {

	private final AtomicBoolean raised = new AtomicBoolean(false);


	/**
	 * Raise this flag indicating a key point in execution.
	 * <br>
	 * If the flag was already raised, this method does nothing.
	 */
	public void raise() {
		raised.set(true);
	}

	/**
	 * Lower this flag that may have been previously raised.
	 * <br>
	 * If the flag was never raised, this method does nothing.
	 */
	public void lower() {
		raised.set(false);
	}

	/**
	 * Check if this flag is raised.
	 *
	 * @return {@code true} if the flag is raised.
	 */
	public boolean isRaised() {
		return raised.get();
	}

	/**
	 * Check if this flag is lowered.
	 *
	 * @return {@code true} if the flag is lowered.
	 */
	public boolean isLowered() {
		return !isRaised();
	}
}
