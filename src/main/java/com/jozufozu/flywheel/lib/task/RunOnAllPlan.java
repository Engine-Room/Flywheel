package com.jozufozu.flywheel.lib.task;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.jozufozu.flywheel.lib.math.MoreMath;

public record RunOnAllPlan<T>(Supplier<List<T>> listSupplier, Consumer<T> action) implements Plan {
	public static <T> Plan of(Supplier<List<T>> iterable, Consumer<T> forEach) {
		return new RunOnAllPlan<>(iterable, forEach);
	}

	@Override
	public void execute(TaskExecutor taskExecutor, Runnable onCompletion) {
		taskExecutor.execute(() -> {
			var list = listSupplier.get();
			final int size = list.size();

			if (size == 0) {
				onCompletion.run();
			} else if (size <= getChunkingThreshold()) {
				processList(list, onCompletion);
			} else {
				dispatchChunks(list, taskExecutor, onCompletion);
			}
		});
	}

	private void dispatchChunks(List<T> suppliedList, TaskExecutor taskExecutor, Runnable onCompletion) {
		final int size = suppliedList.size();
		final int chunkSize = getChunkSize(taskExecutor, size);

		var synchronizer = new Synchronizer(MoreMath.ceilingDiv(size, chunkSize), onCompletion);
		int remaining = size;

		while (remaining > 0) {
			int end = remaining;
			remaining -= chunkSize;
			int start = Math.max(remaining, 0);

			var subList = suppliedList.subList(start, end);
			taskExecutor.execute(() -> processList(subList, synchronizer::decrementAndEventuallyRun));
		}
	}

	private static int getChunkSize(TaskExecutor taskExecutor, int totalSize) {
		return MoreMath.ceilingDiv(totalSize, taskExecutor.getThreadCount() * 32);
	}

	private void processList(List<T> suppliedList, Runnable onCompletion) {
		for (T t : suppliedList) {
			action.accept(t);
		}
		onCompletion.run();
	}

	private static int getChunkingThreshold() {
		return 256;
	}
}
