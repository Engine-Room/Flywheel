package com.jozufozu.flywheel.lib.task;

/**
 * A {@link ContextFunction} that ignores the context object.
 *
 * @param <C> The context type.
 * @param <R> The return type.
 */
@FunctionalInterface
public interface ContextSupplier<C, R> extends ContextFunction<C, R> {
	R get();

	@Override
	default R apply(C ignored) {
		return get();
	}
}
