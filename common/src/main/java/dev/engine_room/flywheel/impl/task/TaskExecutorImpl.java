package dev.engine_room.flywheel.impl.task;

import java.util.function.BooleanSupplier;

import dev.engine_room.flywheel.api.task.TaskExecutor;

public interface TaskExecutorImpl extends TaskExecutor {
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

	/**
	 * Wait for <em>all</em> running tasks to finish.
	 * <br>
	 * This is useful as a nuclear option, but most of the time you should
	 * try to use {@link #syncUntil(BooleanSupplier) syncUntil}.
	 */
	void syncPoint();
}
