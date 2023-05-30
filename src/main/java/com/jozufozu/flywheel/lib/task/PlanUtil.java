package com.jozufozu.flywheel.lib.task;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.jozufozu.flywheel.lib.math.MoreMath;

public class PlanUtil {
	public static <C, T> void distribute(TaskExecutor taskExecutor, C context, Runnable onCompletion, List<T> list, BiConsumer<T, C> action) {
		final int size = list.size();

		if (size == 0) {
			onCompletion.run();
			return;
		}

		final int sliceSize = sliceSize(taskExecutor, size);

		if (size <= sliceSize) {
			for (T t : list) {
				action.accept(t, context);
			}
			onCompletion.run();
		} else if (sliceSize == 1) {
			var synchronizer = new Synchronizer(size, onCompletion);
			for (T t : list) {
				taskExecutor.execute(() -> {
					action.accept(t, context);
					synchronizer.decrementAndEventuallyRun();
				});
			}
		} else {
			var synchronizer = new Synchronizer(MoreMath.ceilingDiv(size, sliceSize), onCompletion);
			int remaining = size;

			while (remaining > 0) {
				int end = remaining;
				remaining -= sliceSize;
				int start = Math.max(remaining, 0);

				var subList = list.subList(start, end);
				taskExecutor.execute(() -> {
					for (T t : subList) {
						action.accept(t, context);
					}
					synchronizer.decrementAndEventuallyRun();
				});
			}
		}
	}

	public static <C, T> void distributeSlices(TaskExecutor taskExecutor, C context, Runnable onCompletion, List<T> list, BiConsumer<List<T>, C> action) {
		final int size = list.size();

		if (size == 0) {
			onCompletion.run();
			return;
		}

		final int sliceSize = sliceSize(taskExecutor, size);

		if (size <= sliceSize) {
			action.accept(list, context);
			onCompletion.run();
		} else if (sliceSize == 1) {
			var synchronizer = new Synchronizer(size, onCompletion);
			for (T t : list) {
				taskExecutor.execute(() -> {
					action.accept(Collections.singletonList(t), context);
					synchronizer.decrementAndEventuallyRun();
				});
			}
		} else {
			var synchronizer = new Synchronizer(MoreMath.ceilingDiv(size, sliceSize), onCompletion);
			int remaining = size;

			while (remaining > 0) {
				int end = remaining;
				remaining -= sliceSize;
				int start = Math.max(remaining, 0);

				var subList = list.subList(start, end);
				taskExecutor.execute(() -> {
					action.accept(subList, context);
					synchronizer.decrementAndEventuallyRun();
				});
			}
		}
	}

	public static int sliceSize(TaskExecutor taskExecutor, int totalSize) {
		return MoreMath.ceilingDiv(totalSize, taskExecutor.getThreadCount() * 32);
	}
}
