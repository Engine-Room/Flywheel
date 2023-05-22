package com.jozufozu.flywheel.lib.task;

import java.util.List;
import java.util.function.BiConsumer;

import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.jozufozu.flywheel.lib.math.MoreMath;

public class PlanUtil {
	public static <C, T> void distribute(TaskExecutor taskExecutor, C context, Runnable onCompletion, List<T> list, BiConsumer<T, C> action) {
		final int size = list.size();

		if (size == 0) {
			onCompletion.run();
		} else if (size <= getChunkSize(taskExecutor, size)) {
			processList(context, onCompletion, list, action);
		} else {
			dispatchChunks(taskExecutor, context, onCompletion, list, action);
		}
	}

	public static int getChunkSize(TaskExecutor taskExecutor, int totalSize) {
		return MoreMath.ceilingDiv(totalSize, taskExecutor.getThreadCount() * 32);
	}

	static <C, T> void dispatchChunks(TaskExecutor taskExecutor, C context, Runnable onCompletion, List<T> list, BiConsumer<T, C> action) {
		final int size = list.size();
		final int chunkSize = getChunkSize(taskExecutor, size);

		var synchronizer = new Synchronizer(MoreMath.ceilingDiv(size, chunkSize), onCompletion);
		int remaining = size;

		while (remaining > 0) {
			int end = remaining;
			remaining -= chunkSize;
			int start = Math.max(remaining, 0);

			var subList = list.subList(start, end);
			taskExecutor.execute(() -> processList(context, synchronizer, subList, action));
		}
	}

	static <C, T> void processList(C context, Runnable onCompletion, List<T> list, BiConsumer<T, C> action) {
		for (var t : list) {
			action.accept(t, context);
		}
		onCompletion.run();
	}
}
