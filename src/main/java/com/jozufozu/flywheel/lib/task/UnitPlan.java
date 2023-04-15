package com.jozufozu.flywheel.lib.task;

import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;

public class UnitPlan implements Plan {
	public static final UnitPlan INSTANCE = new UnitPlan();

	private UnitPlan() {
	}

	@Override
	public void execute(TaskExecutor taskExecutor, Runnable onCompletion) {
		onCompletion.run();
	}
}
