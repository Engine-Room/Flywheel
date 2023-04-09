package com.jozufozu.flywheel.lib.task;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;

public record RunOnAllPlan<T>(Supplier<List<T>> listSupplier, Consumer<T> action) implements Plan {
	@Override
	public void execute(TaskExecutor taskExecutor, Runnable onCompletion) {
		// TODO: unit tests, fix CME?
		taskExecutor.execute(() -> {
			var list = listSupplier.get();
			final int size = list.size();

			if (size == 0) {
				onCompletion.run();
			} else if (size <= getChunkingThreshold(taskExecutor)) {
				processList(list, onCompletion);
			} else {
				dispatchChunks(list, taskExecutor, onCompletion);
			}
		});
	}

	private void dispatchChunks(List<T> suppliedList, TaskExecutor taskExecutor, Runnable onCompletion) {
		final int size = suppliedList.size();
		final int threadCount = taskExecutor.getThreadCount();

		final int chunkSize = (size + threadCount - 1) / threadCount; // ceiling division
		final int chunkCount = (size + chunkSize - 1) / chunkSize; // ceiling division

		var synchronizer = new Synchronizer(chunkCount, onCompletion);
		int remaining = size;

		while (remaining > 0) {
			int end = remaining;
			remaining -= chunkSize;
			int start = Math.max(remaining, 0);

			var subList = suppliedList.subList(start, end);
			taskExecutor.execute(() -> processList(subList, synchronizer::decrementAndEventuallyRun));
		}
	}

	private void processList(List<T> suppliedList, Runnable onCompletion) {
		for (T t : suppliedList) {
			action.accept(t);
		}
		onCompletion.run();
	}

	private static int getChunkingThreshold(TaskExecutor taskExecutor) {
		return 512;
	}
}
