package com.jozufozu.flywheel.impl.visualization.storage;

import java.util.ArrayList;
import java.util.List;

import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.jozufozu.flywheel.lib.task.PlanUtil;
import com.jozufozu.flywheel.lib.task.SimplyComposedPlan;
import com.jozufozu.flywheel.lib.task.Synchronizer;

public class PlanStorage<T, C> implements SimplyComposedPlan<C> {
	private final List<T> objects = new ArrayList<>();
	private final List<Plan<C>> plans = new ArrayList<>();

	public void add(T object, Plan<C> plan) {
		objects.add(object);
		plans.add(plan);
	}

	public void remove(T object) {
		int index = objects.indexOf(object);

		if (index != -1) {
			objects.remove(index);
			plans.remove(index);
		}
	}

	public void clear() {
		objects.clear();
		plans.clear();
	}

	@Override
	public void execute(TaskExecutor taskExecutor, C context, Runnable onCompletion) {
		final int size = plans.size();

		if (size == 0) {
			onCompletion.run();
			return;
		}

		var synchronizer = new Synchronizer(size, onCompletion);
		final int sliceSize = PlanUtil.sliceSize(taskExecutor, size, 8);

		if (size <= sliceSize) {
			for (var t : plans) {
				t.execute(taskExecutor, context, synchronizer);
			}
		} else if (sliceSize == 1) {
			for (var t : plans) {
				taskExecutor.execute(() -> t.execute(taskExecutor, context, synchronizer));
			}
		} else {
			int remaining = size;

			while (remaining > 0) {
				int end = remaining;
				remaining -= sliceSize;
				int start = Math.max(remaining, 0);

				var subList = plans.subList(start, end);
				taskExecutor.execute(() -> {
					for (var t : subList) {
						t.execute(taskExecutor, context, synchronizer);
					}
				});
			}
		}
	}
}
