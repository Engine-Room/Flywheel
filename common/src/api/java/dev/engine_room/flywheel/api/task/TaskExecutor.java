package dev.engine_room.flywheel.api.task;

import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface TaskExecutor extends Executor {

	/**
	 * Schedule a task to be run on the main thread.
	 * <br>
	 * This method may be called from any thread (including the main thread),
	 * but the runnable will <em>only</em> be executed once somebody calls
	 * {@link #syncUntil(BooleanSupplier)} on this task executor's main thread.
	 * @param runnable The task to run.
	 */
	void scheduleForMainThread(Runnable runnable);

	/**
	 * Check whether the current thread is this task executor's main thread.
	 *
	 * @return {@code true} if the current thread is the main thread.
	 */
	boolean isMainThread();

	/**
	 * Check for the number of threads this executor uses.
	 * <br>
	 * May be helpful when determining how many chunks to divide a task into.
	 *
	 * @return The number of threads this executor uses.
	 */
	int threadCount();

	/**
	 * Wait for running tasks, until the given condition is met
	 * ({@link BooleanSupplier#getAsBoolean()} returns {@code true}).
	 * <br>
	 * This method is equivalent to {@code syncWhile(() -> !cond.getAsBoolean())}.
	 *
	 * @param cond The condition to wait for.
	 * @return {@code true} if the condition is met. {@code false} if
	 * this executor runs out of tasks before the condition is met.
	 */
	boolean syncUntil(BooleanSupplier cond);

	/**
	 * Wait for running tasks, so long as the given condition is met
	 * ({@link BooleanSupplier#getAsBoolean()} returns {@code true}).
	 * <br>
	 * If this method is called on the
	 * <br>
	 * This method is equivalent to {@code syncUntil(() -> !cond.getAsBoolean())}.
	 *
	 * @param cond The condition sync on.
	 * @return {@code true} if the condition is no longer met. {@code false} if
	 * this executor runs out of tasks while the condition is still met.
	 */
	boolean syncWhile(BooleanSupplier cond);
}
