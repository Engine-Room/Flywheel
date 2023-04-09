package com.jozufozu.flywheel.lib.task;

import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;

public record BarrierPlan(Plan first, Plan second) implements Plan {
	@Override
	public void execute(TaskExecutor taskExecutor, Runnable onCompletion) {
		first.execute(taskExecutor, () -> second.execute(taskExecutor, onCompletion));
	}
}
