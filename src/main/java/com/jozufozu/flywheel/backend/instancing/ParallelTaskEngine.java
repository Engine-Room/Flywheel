package com.jozufozu.flywheel.backend.instancing;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.backend.instancing.batching.WaitGroup;

import net.minecraft.util.Mth;

// https://github.com/CaffeineMC/sodium-fabric/blob/5d364ed5ba63f9067fcf72a078ca310bff4db3e9/src/main/java/me/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuilder.java
public class ParallelTaskEngine implements TaskEngine {
	private static final Logger LOGGER = LoggerFactory.getLogger("BatchExecutor");

	private final String name;

	/**
	 * If set to false, the engine will shut down.
	 */
	private final AtomicBoolean running = new AtomicBoolean(false);
	private final WaitGroup wg = new WaitGroup();

	private final Deque<Runnable> syncTasks = new ConcurrentLinkedDeque<>();
	private final Deque<Runnable> jobQueue = new ConcurrentLinkedDeque<>();
	private final List<Thread> threads = new ArrayList<>();

	private final Object jobNotifier = new Object();

	private final int threadCount;

	public ParallelTaskEngine(String name) {
		this.name = name;
		threadCount = getOptimalThreadCount();
	}

	public WorkGroupBuilder group(String name) {
		return new WorkGroupBuilder(name);
	}

	/**
	 * Spawns a number of work-stealing threads to process results in the build queue. If the builder is already
	 * running, this method does nothing and exits.
	 */
	public void startWorkers() {
		if (this.running.getAndSet(true)) {
			return;
		}

		if (!this.threads.isEmpty()) {
			throw new IllegalStateException("Threads are still alive while in the STOPPED state");
		}

		for (int i = 0; i < this.threadCount; i++) {

			Thread thread = new Thread(new WorkerRunnable(), name + " " + i);
			thread.setPriority(Math.max(0, Thread.NORM_PRIORITY - 2));
			thread.start();

			this.threads.add(thread);
		}

		LOGGER.info("Started {} worker threads", this.threads.size());
	}

	public void stopWorkers() {
		if (!this.running.getAndSet(false)) {
			return;
		}

		if (this.threads.isEmpty()) {
			throw new IllegalStateException("No threads are alive but the executor is in the RUNNING state");
		}

		synchronized (this.jobNotifier) {
			this.jobNotifier.notifyAll();
		}

		try {
			for (Thread thread : this.threads) {
				thread.join();
			}
		} catch (InterruptedException ignored) {
		}

		this.threads.clear();

		this.jobQueue.clear();
	}

	/**
	 * Submit a task to the pool.
	 */
	@Override
	public void submit(@NotNull Runnable command) {
		this.jobQueue.add(command);
		this.wg.add(1);

		synchronized (this.jobNotifier) {
			this.jobNotifier.notify();
		}
	}

	/**
	 * Wait for all running jobs to finish.
	 */
	@Override
	public void syncPoint() {
		Runnable job;

		// Finish everyone else's work...
		while ((job = this.jobQueue.pollLast()) != null) {
			processTask(job);
		}

		// and wait for any stragglers.
		try {
			this.wg.await();
		} catch (InterruptedException ignored) {
		}

		while ((job = this.syncTasks.pollLast()) != null) {
			job.run();
		}
	}

	@Nullable
	private Runnable getNextTask() {
		Runnable job = this.jobQueue.pollFirst();

		if (job == null) {
			synchronized (ParallelTaskEngine.this.jobNotifier) {
				try {
					ParallelTaskEngine.this.jobNotifier.wait();
				} catch (InterruptedException ignored) {
				}
			}
		}

		return job;
	}

	// TODO: job context
	private void processTask(Runnable job) {
		try {
			job.run();
		} catch (Exception e) {
			Flywheel.LOGGER.error("Error running job", e);
		} finally {
			ParallelTaskEngine.this.wg.done();
		}
	}

	/**
	 * Returns the "optimal" number of threads to be used for chunk build tasks. This will always return at least one
	 * thread.
	 */
	private static int getOptimalThreadCount() {
		return Mth.clamp(Math.max(getMaxThreadCount() / 3, getMaxThreadCount() - 6), 1, 10);
	}

	private static int getMaxThreadCount() {
		return Runtime.getRuntime().availableProcessors();
	}

	private class WorkerRunnable implements Runnable {

		private final AtomicBoolean running = ParallelTaskEngine.this.running;

		@Override
		public void run() {
			// Run until the chunk builder shuts down
			while (this.running.get()) {
				Runnable job = ParallelTaskEngine.this.getNextTask();

				if (job == null) {
					continue;
				}

				ParallelTaskEngine.this.processTask(job);
			}
		}

	}

	public class WorkGroupBuilder {
		final String name;

		@Nullable
		Runnable finalizer;

		Stream<Runnable> tasks;

		public WorkGroupBuilder(String name) {
			this.name = name;
		}

		public <T> WorkGroupBuilder addTasks(Stream<T> iterable, Consumer<T> consumer) {
			return addTasks(iterable.map(it -> () -> consumer.accept(it)));
		}

		public WorkGroupBuilder addTasks(Stream<Runnable> tasks) {
			if (this.tasks == null) {
				this.tasks = tasks;
			} else {
				this.tasks = Stream.concat(this.tasks, tasks);
			}
			return this;
		}

		public WorkGroupBuilder onComplete(Runnable runnable) {
			this.finalizer = runnable;
			return this;
		}

		public void submit() {
			if (this.tasks == null) {
				return;
			}

			WorkGroup workGroup = new WorkGroup(name, finalizer);

			tasks.map(task -> new WorkGroupTask(workGroup, task)).forEach(ParallelTaskEngine.this::submit);
		}

	}

	private static class WorkGroupTask implements Runnable {

		private final WorkGroup parent;
		private final Runnable wrapped;

		public WorkGroupTask(WorkGroup parent, Runnable wrapped) {
			this.parent = parent;
			this.wrapped = wrapped;

			this.parent.running.incrementAndGet();
		}

		@Override
		public void run() {
			this.wrapped.run();

			this.parent.oneDown();
		}
	}

	private class WorkGroup {
		final String name;

		final Runnable finalizer;

		final AtomicInteger running = new AtomicInteger(0);

		public WorkGroup(String name, @Nullable Runnable finalizer) {
			this.name = name;
			this.finalizer = finalizer;
		}

		public void oneDown() {
			if (finalizer != null) {
				if (running.decrementAndGet() == 0) {
					ParallelTaskEngine.this.syncTasks.add(finalizer);
				}
			}
		}
	}
}
