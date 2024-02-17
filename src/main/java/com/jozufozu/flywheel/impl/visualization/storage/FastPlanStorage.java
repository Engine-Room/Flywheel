package com.jozufozu.flywheel.impl.visualization.storage;

import java.util.ArrayList;
import java.util.List;

import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.jozufozu.flywheel.lib.task.PlanUtil;
import com.jozufozu.flywheel.lib.task.SimplyComposedPlan;
import com.jozufozu.flywheel.lib.task.functional.ConsumerWithContext;

public class FastPlanStorage<T, C> implements SimplyComposedPlan<C> {
	private final List<T> objects = new ArrayList<>();
	private final ConsumerWithContext<T, C> consumer;

	public FastPlanStorage(ConsumerWithContext<T, C> consumer) {
		this.consumer = consumer;
	}

	public void add(T object) {
		objects.add(object);
	}

	public void remove(T object) {
		objects.remove(object);
	}

	public void clear() {
		objects.clear();
	}

	@Override
	public void execute(TaskExecutor taskExecutor, C context, Runnable onCompletion) {
		PlanUtil.distribute(taskExecutor, context, onCompletion, objects, consumer);
	}
}
