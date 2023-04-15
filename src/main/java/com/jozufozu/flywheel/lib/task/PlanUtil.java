package com.jozufozu.flywheel.lib.task;

import java.util.List;

import com.jozufozu.flywheel.api.task.Plan;

public class PlanUtil {

	public static Plan of() {
		return UnitPlan.INSTANCE;
	}

	public static Plan of(Plan... plans) {
		return new NestedPlan(List.of(plans));
	}

	public static Plan of(Runnable... tasks) {
		return SimplePlan.of(tasks);
	}

	public static Plan onMainThread(Runnable task) {
		return new OnMainThreadPlan(task);
	}
}
