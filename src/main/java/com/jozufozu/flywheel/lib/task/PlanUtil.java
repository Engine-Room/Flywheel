package com.jozufozu.flywheel.lib.task;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.jozufozu.flywheel.api.task.Plan;

public class PlanUtil {
	public static <T> Plan runOnAll(Supplier<List<T>> iterable, Consumer<T> forEach) {
		return new RunOnAllPlan<>(iterable, forEach);
	}

	public static Plan of() {
		return UnitPlan.INSTANCE;
	}

	public static Plan of(Plan... plans) {
		return new NestedPlan(List.of(plans));
	}

	public static Plan of(Runnable... tasks) {
		return new SimplePlan(List.of(tasks));
	}

	public static Plan onMainThread(Runnable task) {
		return new OnMainThreadPlan(task);
	}
}
