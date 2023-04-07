package com.jozufozu.flywheel.backend.task;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jetbrains.annotations.Nullable;
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

	/**
	 * If set to false, the executor will shut down.
	 */
	private final AtomicBoolean running = new AtomicBoolean(false);
	/**
	 * Synchronized via {@link #tasksCompletedNotifier}.
	 */
	private int incompleteTaskCounter = 0;

	private final List<WorkerThread> threads = new ArrayList<>();
	private final Deque<Runnable> taskQueue = new ConcurrentLinkedDeque<>();

	private final Object taskNotifier = new Object();
	private final Object tasksCompletedNotifier = new Object();

	public ParallelTaskExecutor(String name) {
		this.name = name;
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
		synchronized (tasksCompletedNotifier) {
			incompleteTaskCounter = 0;
			tasksCompletedNotifier.notifyAll();
		}
	}

	@Override
	public void execute(Runnable task) {
		if (!running.get()) {
			throw new IllegalStateException("Executor is stopped");
		}

		taskQueue.add(task);

		synchronized (tasksCompletedNotifier) {
			incompleteTaskCounter++;
		}

		synchronized (taskNotifier) {
			taskNotifier.notify();
		}
	}

	/**
	 * Wait for all running tasks to finish.
	 */
	@Override
	public void syncPoint() {
		Runnable task;

		// Finish everyone else's work...
		while ((task = taskQueue.pollLast()) != null) {
			processTask(task);
		}

		// and wait for any stragglers.
		synchronized (tasksCompletedNotifier) {
			while (incompleteTaskCounter > 0) {
				try {
					tasksCompletedNotifier.wait();
				} catch (InterruptedException e) {
					//
				}
			}
		}
	}

	public void discardAndAwait() {
		// Discard everyone else's work...
		while (taskQueue.pollLast() != null) {
			synchronized (tasksCompletedNotifier) {
				if (--incompleteTaskCounter == 0) {
					tasksCompletedNotifier.notifyAll();
				}
			}
		}

		// and wait for any stragglers.
		synchronized (tasksCompletedNotifier) {
			while (incompleteTaskCounter > 0) {
				try {
					tasksCompletedNotifier.wait();
				} catch (InterruptedException e) {
					//
				}
			}
		}
	}

	@Nullable
	private Runnable getNextTask() {
		Runnable task = taskQueue.pollFirst();

		if (task == null) {
			synchronized (taskNotifier) {
				try {
					taskNotifier.wait();
				} catch (InterruptedException e) {
					//
				}
			}
		}

		return task;
	}

	// TODO: task context
	private void processTask(Runnable task) {
		try {
			task.run();
		} catch (Exception e) {
			Flywheel.LOGGER.error("Error running task", e);
		} finally {
			synchronized (tasksCompletedNotifier) {
				if (--incompleteTaskCounter == 0) {
					tasksCompletedNotifier.notifyAll();
				}
			}
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
		private final AtomicBoolean running = ParallelTaskExecutor.this.running;

		public WorkerThread(String name) {
			super(name);
		}

		@Override
		public void run() {
			// Run until the executor shuts down
			while (running.get()) {
				Runnable task = getNextTask();

				if (task == null) {
					continue;
				}

				processTask(task);
			}
		}
	}
}
