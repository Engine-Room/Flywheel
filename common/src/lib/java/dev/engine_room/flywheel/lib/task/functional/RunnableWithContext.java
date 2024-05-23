package dev.engine_room.flywheel.lib.task.functional;

import java.util.function.Consumer;

/**
 * A runnable like interface for use with {@link dev.engine_room.flywheel.api.task.Plan Plans} and their contexts.
 * <br>
 * The subinterface {@link Ignored} is provided for runnables that do not need the context object.
 * @param <C> The context type.
 */
@FunctionalInterface
public interface RunnableWithContext<C> extends Consumer<C> {
	void run(C context);

	@Override
	default void accept(C c) {
		run(c);
	}

	/**
	 * A {@link RunnableWithContext} that ignores the context object.
	 *
	 * @param <C> The (ignored) context type.
	 */
	@FunctionalInterface
	interface Ignored<C> extends RunnableWithContext<C>, Runnable {
		@Override
		void run();

		@Override
		default void run(C ignored) {
			run();
		}
	}
}
