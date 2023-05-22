package com.jozufozu.flywheel.lib.task;

import com.jozufozu.flywheel.api.task.Plan;

/**
 * A consumer like interface for use with {@link Plan}s.
 *
 * @param <C> The context type.
 */
@FunctionalInterface
public interface ContextConsumer<C> {
	void accept(C context);
}
