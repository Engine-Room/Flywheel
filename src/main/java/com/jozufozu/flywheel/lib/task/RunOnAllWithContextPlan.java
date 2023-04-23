package com.jozufozu.flywheel.lib.task;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.jozufozu.flywheel.lib.math.MoreMath;

public record RunOnAllWithContextPlan<T, C>(Supplier<List<T>> listSupplier,
											BiConsumer<T, C> action) implements SimplyComposedPlan<C> {
	public static <T, C> Plan<C> of(Supplier<List<T>> iterable, BiConsumer<T, C> forEach) {
		return new RunOnAllWithContextPlan<>(iterable, forEach);
	}

	@Override
	public void execute(TaskExecutor taskExecutor, C context, Runnable onCompletion) {
		taskExecutor.execute(() -> {
			var list = listSupplier.get();
			final int size = list.size();

			if (size == 0) {
				onCompletion.run();
			} else if (size <= getChunkingThreshold()) {
				processList(list, context, onCompletion);
			} else {
				dispatchChunks(list, taskExecutor, context, onCompletion);
			}
		});
	}

	private void dispatchChunks(List<T> suppliedList, TaskExecutor taskExecutor, C context, Runnable onCompletion) {
		final int size = suppliedList.size();
		final int chunkSize = getChunkSize(taskExecutor, size);

		var synchronizer = new Synchronizer(MoreMath.ceilingDiv(size, chunkSize), onCompletion);
		int remaining = size;

		while (remaining > 0) {
			int end = remaining;
			remaining -= chunkSize;
			int start = Math.max(remaining, 0);

			var subList = suppliedList.subList(start, end);
			taskExecutor.execute(() -> processList(subList, context, synchronizer));
		}
	}

	private static int getChunkSize(TaskExecutor taskExecutor, int totalSize) {
		return MoreMath.ceilingDiv(totalSize, taskExecutor.getThreadCount() * 32);
	}

	private void processList(List<T> suppliedList, C context, Runnable onCompletion) {
		for (T t : suppliedList) {
			action.accept(t, context);
		}
		onCompletion.run();
	}

	private static int getChunkingThreshold() {
		return 256;
	}
}
