package com.jozufozu.flywheel.lib.task;

import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

public class WorkGroup {
	public static void run(Iterator<Runnable> tasks, Executor executor) {
		tasks.forEachRemaining(executor::execute);
	}

	public static void run(Iterator<Runnable> tasks, @Nullable Runnable finalizer, Executor executor) {
		if (finalizer == null) {
			run(tasks, executor);
			return;
		}

		AtomicInteger incompleteTaskCounter = new AtomicInteger(0);
		tasks.forEachRemaining(task -> {
			incompleteTaskCounter.incrementAndGet();
			executor.execute(() -> {
				task.run();
				if (incompleteTaskCounter.decrementAndGet() == 0) {
					executor.execute(finalizer);
				}
			});
		});
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		@Nullable
		private Runnable finalizer;
		private Stream<Runnable> tasks;

		public Builder() {
		}

		public <T> Builder addTasks(Stream<T> iterable, Consumer<T> consumer) {
			return addTasks(iterable.map(it -> () -> consumer.accept(it)));
		}

		public Builder addTasks(Stream<Runnable> tasks) {
			if (this.tasks == null) {
				this.tasks = tasks;
			} else {
				this.tasks = Stream.concat(this.tasks, tasks);
			}
			return this;
		}

		public Builder onComplete(Runnable runnable) {
			this.finalizer = runnable;
			return this;
		}

		public void execute(Executor executor) {
			if (tasks == null) {
				return;
			}

			run(tasks.iterator(), finalizer, executor);
		}
	}
}
