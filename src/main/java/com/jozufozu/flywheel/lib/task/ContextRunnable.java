package com.jozufozu.flywheel.lib.task;

/**
 * A {@link ContextConsumer} that ignores the context object.
 *
 * @param <C> The context type.
 */
@FunctionalInterface
public interface ContextRunnable<C> extends ContextConsumer<C> {
	void run();

	@Override
	default void accept(C ignored) {
		run();
	}
}
