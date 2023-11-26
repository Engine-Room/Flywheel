package com.jozufozu.flywheel.lib.task;

import com.jozufozu.flywheel.api.task.Plan;

/**
 * A function like interface for use with {@link Plan}s.
 * @param <C> The context type.
 * @param <R> The return type.
 */
@FunctionalInterface
public interface ContextFunction<C, R> {
	R apply(C context);
}
