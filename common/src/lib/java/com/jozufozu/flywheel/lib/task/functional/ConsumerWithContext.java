package com.jozufozu.flywheel.lib.task.functional;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A consumer like interface for use with {@link com.jozufozu.flywheel.api.task.Plan Plans} and their contexts.
 * <br>
 * The subinterface {@link Ignored} is provided for consumers that do not need the context object.
 *
 * @param <T> The type to actually consume.
 * @param <C> The context type.
 */
@FunctionalInterface
public interface ConsumerWithContext<T, C> extends BiConsumer<T, C> {
	void accept(T t, C context);

	/**
	 * A {@link ConsumerWithContext} that ignores the context object.
	 *
	 * @param <T> The type to actually consume.
	 * @param <C> The (ignored) context type.
	 */
	@FunctionalInterface
	interface Ignored<T, C> extends ConsumerWithContext<T, C>, Consumer<T> {
		@Override
		void accept(T t);

		@Override
		default void accept(T t, C ignored) {
			accept(t);
		}
	}
}
