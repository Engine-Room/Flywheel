package dev.engine_room.flywheel.impl.task;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

import dev.engine_room.flywheel.impl.FlwImpl;
import net.minecraft.util.Mth;

// https://github.com/CaffeineMC/sodium-fabric/blob/5d364ed5ba63f9067fcf72a078ca310bff4db3e9/src/main/java/me/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuilder.java
// https://stackoverflow.com/questions/29655531
public class ParallelTaskExecutor implements TaskExecutorImpl {
	private final String name;
	private final int threadCount;

	/**
	 * If set to false, the executor will shut down.
	 */
	private final AtomicBoolean running = new AtomicBoolean(false);

	private final List<WorkerThread> threads = new ArrayList<>();
	private final Deque<Runnable> taskQueue = new ConcurrentLinkedDeque<>();
	private final ThreadGroupNotifier taskNotifier = new ThreadGroupNotifier();
	private final WaitGroup waitGroup = new WaitGroup();

	public ParallelTaskExecutor(String name, int threadCount) {
		this.name = name;
		this.threadCount = threadCount;
	}

	@Override
	public int threadCount() {
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

		FlwImpl.LOGGER.info("Started {} worker threads", threads.size());
	}

	public void stopWorkers() {
		if (!running.getAndSet(false)) {
			return;
		}

		if (threads.isEmpty()) {
			throw new IllegalStateException("No threads are alive but the executor is in the RUNNING state");
		}

		FlwImpl.LOGGER.info("Stopping worker threads");

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
	public boolean syncUntil(BooleanSupplier cond) {
		while (true) {
			if (cond.getAsBoolean()) {
				// The condition is already true!
				// Early return with true to indicate.
				return true;
			}

			if (syncOneTask()) {
				// Out of tasks entirely.
				// The condition may have flipped though so return its result.
				return cond.getAsBoolean();
			}
		}
	}

	@Override
	public boolean syncWhile(BooleanSupplier cond) {
		while (true) {
			if (!cond.getAsBoolean()) {
				// The condition is already false!
				// Early return with true to indicate.
				return true;
			}

			if (syncOneTask()) {
				// Out of tasks entirely.
				// The condition may have flipped though so return its result.
				return !cond.getAsBoolean();
			}
		}
	}

	@Override
	public void syncPoint() {
		while (true) {
			if (syncOneTask()) {
				// Done! Nothing left to do.
				return;
			}
		}
	}

	/**
	 * Attempt to process a single task.
	 *
	 * @return {@code true} if the executor has nothing left to do.
	 */
	private boolean syncOneTask() {
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
			FlwImpl.LOGGER.error("Error running task", e);
		} finally {
			waitGroup.done();
		}
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
