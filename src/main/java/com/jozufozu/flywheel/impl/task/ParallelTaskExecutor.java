package com.jozufozu.flywheel.impl.task;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

import org.slf4j.Logger;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.mojang.logging.LogUtils;

import net.minecraft.util.Mth;

// https://github.com/CaffeineMC/sodium-fabric/blob/5d364ed5ba63f9067fcf72a078ca310bff4db3e9/src/main/java/me/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuilder.java
// https://stackoverflow.com/questions/29655531
public class ParallelTaskExecutor implements TaskExecutor {
	private static final Logger LOGGER = LogUtils.getLogger();

	private final String name;
	private final int threadCount;

	private final BooleanSupplier mainThreadQuery;

	/**
	 * If set to false, the executor will shut down.
	 */
	private final AtomicBoolean running = new AtomicBoolean(false);

	private final List<WorkerThread> threads = new ArrayList<>();
	private final Deque<Runnable> taskQueue = new ConcurrentLinkedDeque<>();
	private final Queue<Runnable> mainThreadQueue = new ConcurrentLinkedQueue<>();
	private final ThreadGroupNotifier taskNotifier = new ThreadGroupNotifier();
	private final WaitGroup waitGroup = new WaitGroup();

	public ParallelTaskExecutor(String name, BooleanSupplier mainThreadQuery) {
		this.name = name;
		this.mainThreadQuery = mainThreadQuery;
		threadCount = getOptimalThreadCount();
	}

	@Override
	public int getThreadCount() {
		return threadCount;
	}

	/**
	 * Spawns a number of work-stealing threads to process results in the task queue. If the executor is already
	 * running, this method does nothing and exits.
	 */
	public void startWorkers() {
		if (running.getAndSet(true)) {
			return;
		}

		if (!threads.isEmpty()) {
			throw new IllegalStateException("Threads are still alive while in the STOPPED state");
		}

		for (int i = 0; i < threadCount; i++) {
			WorkerThread thread = new WorkerThread(name + " Task Executor #" + i);
			thread.setPriority(Mth.clamp(Thread.NORM_PRIORITY - 2, Thread.MIN_PRIORITY, Thread.MAX_PRIORITY));
			thread.start();

			threads.add(thread);
		}

		LOGGER.info("Started {} worker threads", threads.size());
	}

	public void stopWorkers() {
		if (!running.getAndSet(false)) {
			return;
		}

		if (threads.isEmpty()) {
			throw new IllegalStateException("No threads are alive but the executor is in the RUNNING state");
		}

		LOGGER.info("Stopping worker threads");

		// Notify all worker threads to wake up, where they will then terminate
		synchronized (taskNotifier) {
			taskNotifier.notifyAll();
		}

		// Wait for every remaining thread to terminate
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				//
			}
		}

		threads.clear();
		taskQueue.clear();
		mainThreadQueue.clear();
		waitGroup._reset();
	}

	@Override
	public void execute(Runnable task) {
		if (!running.get()) {
			throw new IllegalStateException("Executor is stopped");
		}

		waitGroup.add();
		taskQueue.add(task);

		taskNotifier.postNotification();
	}

	@Override
	public void scheduleForMainThread(Runnable runnable) {
		if (!running.get()) {
			throw new IllegalStateException("Executor is stopped");
		}

		mainThreadQueue.add(runnable);
	}

	@Override
	public boolean isMainThread() {
		return mainThreadQuery.getAsBoolean();
	}

	/**
	 * Wait for all running tasks to finish.
	 */
	@Override
	public void syncPoint() {
		boolean onMainThread = isMainThread();
		while (true) {
			if (syncOneTask(onMainThread)) {
				// Done! Nothing left to do.
				return;
			}
		}
	}

	@Override
	public boolean syncUntil(BooleanSupplier cond) {
		boolean onMainThread = isMainThread();
		while (true) {
			if (cond.getAsBoolean()) {
				// The condition is already true!
				// Early return with true to indicate.
				return true;
			}

			if (syncOneTask(onMainThread)) {
				// Out of tasks entirely.
				// The condition may have flipped though so return its result.
				return cond.getAsBoolean();
			}
		}
	}


	@Override
	public boolean syncWhile(BooleanSupplier cond) {
		boolean onMainThread = isMainThread();
		while (true) {
			if (!cond.getAsBoolean()) {
				// The condition is already false!
				// Early return with true to indicate.
				return true;
			}

			if (syncOneTask(onMainThread)) {
				// Out of tasks entirely.
				// The condition may have flipped though so return its result.
				return !cond.getAsBoolean();
			}
		}
	}

	/**
	 * Attempt to process a single task.
	 *
	 * @param mainThread Whether this is being called from the main thread or not.
	 * @return {@code true} if the executor has nothing left to do.
	 */
	private boolean syncOneTask(boolean mainThread) {
		return mainThread ? syncOneTaskMainThread() : syncOneTaskOffThread();
	}

	private boolean syncOneTaskMainThread() {
		Runnable task;
		if ((task = mainThreadQueue.poll()) != null) {
			// Prioritize main thread tasks.
			processMainThreadTask(task);

			// Check again next loop.
			return false;
		} else if ((task = taskQueue.pollLast()) != null) {
			// Nothing in the mainThreadQueue, work on tasks from the normal queue.
			processTask(task);

			// Check again next loop.
			return false;
		} else {
			// Nothing right now, wait for the other threads to finish.
			boolean done = waitGroup.await(10_000);
			// If we timed-out tasks may have been added to the queue, so check again.
			// if they didn't, we're done.
			return done && mainThreadQueue.isEmpty();
		}
	}

	private boolean syncOneTaskOffThread() {
		Runnable task;
		if ((task = taskQueue.pollLast()) != null) {
			// then work on tasks from the queue.
			processTask(task);
			// Check again next loop.
			return false;
		} else {
			// Nothing right now, wait for the other threads to finish.
			// If we timed-out tasks may have been added to the queue, so check again.
			// if they didn't, we're done.
			return waitGroup.await(10_000);
		}
	}

	private void processTask(Runnable task) {
		try {
			task.run();
		} catch (Exception e) {
			Flywheel.LOGGER.error("Error running task", e);
		} finally {
			waitGroup.done();
		}
	}

	private void processMainThreadTask(Runnable task) {
		try {
			task.run();
		} catch (Exception e) {
			Flywheel.LOGGER.error("Error running main thread task", e);
		}
	}

	/**
	 * Returns the "optimal" number of threads to be used for tasks. This will always return at least one thread.
	 */
	private static int getOptimalThreadCount() {
		return Mth.clamp(Math.max(getMaxThreadCount() / 3, getMaxThreadCount() - 6), 1, 10);
	}

	private static int getMaxThreadCount() {
		return Runtime.getRuntime().availableProcessors();
	}

	private class WorkerThread extends Thread {
		public WorkerThread(String name) {
			super(name);
		}

		@Override
		public void run() {
			// Run until the executor shuts down
			while (ParallelTaskExecutor.this.running.get()) {
				Runnable task = taskQueue.pollFirst();

				if (task != null) {
					processTask(task);
				} else {
					// Nothing to do, time to sleep.
					spinThenWait();
				}
			}
		}

		private void spinThenWait() {
			var waitStart = System.nanoTime();

			// Spin for .01ms before waiting to reduce latency in narrow conditions.
			while (System.nanoTime() - waitStart < 10_000) {
				if (!taskQueue.isEmpty()) {
					// Nice! Exit without waiting.
					return;
				}

				Thread.onSpinWait();
			}
			taskNotifier.awaitNotification();
		}
	}
}
