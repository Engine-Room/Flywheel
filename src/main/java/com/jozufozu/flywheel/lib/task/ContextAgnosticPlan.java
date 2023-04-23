package com.jozufozu.flywheel.lib.task;

import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;

public interface ContextAgnosticPlan extends SimplyComposedPlan<Object> {
	@SuppressWarnings("unchecked")
	default <C> Plan<C> cast() {
		// The context is entirely ignored, so we can safely cast to any context.
		return (Plan<C>) this;
	}

	@Override
	default void execute(TaskExecutor taskExecutor, Object ignored, Runnable onCompletion) {
		execute(taskExecutor, onCompletion);
	}

	void execute(TaskExecutor taskExecutor, Runnable onCompletion);
}
