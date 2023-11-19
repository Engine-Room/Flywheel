package com.jozufozu.flywheel.api.task;

import java.util.concurrent.Executor;

public interface TaskExecutor extends Executor {
	/**
	 * Wait for <em>all</em> running tasks to finish.
	 * <br>
	 * This is useful as a nuclear option, but most of the time you should
	 * try to use {@link Flag flags} and {@link #syncTo(Flag) syncTo}.
	 */
	void syncPoint();

	/**
	 * Wait for running tasks, until the given Flag is {@link #raise raised}.
	 * <br>
	 * The flag will remain raised until {@link #lower lowered} manually.
	 *
	 * @param flag The flag to wait for.
	 * @return {@code true} if the flag was encountered. May return false if
	 * this executor runs out of tasks before the flag was raised.
	 */
	boolean syncTo(Flag flag);

	/**
	 * Raise a flag indicating a key point in execution.
	 * <br>
	 * If the flag was already raised, this method does nothing.
	 *
	 * @param flag The flag to raise.
	 */
	void raise(Flag flag);

	/**
	 * Lower a flag that may have been previously raised.
	 * <br>
	 * If the flag was never raised, this method does nothing.
	 *
	 * @param flag The flag to lower.
	 */
	void lower(Flag flag);

	/**
	 * Check if a flag is raised without waiting for it.
	 *
	 * @param flag The flag to check.
	 * @return {@code true} if the flag is raised.
	 */
	boolean isRaised(Flag flag);

	/**
	 * Check for the number of threads this executor uses.
	 * <br>
	 * May be helpful when determining how many chunks to divide a task into.
	 *
	 * @return The number of threads this executor uses.
	 */
	int getThreadCount();

	/**
	 * Schedule a task to be run on the main thread.
	 * <br>
	 * This method may be called from any thread, but the runnable will only
	 * be executed once somebody calls either {@link #syncPoint()} or
	 * {@link #syncTo(Flag)}.
	 * @param runnable The task to run.
	 */
	void scheduleForSync(Runnable runnable);
}
