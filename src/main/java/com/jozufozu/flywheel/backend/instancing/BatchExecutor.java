package com.jozufozu.flywheel.backend.instancing;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.backend.instancing.batching.WaitGroup;

import net.minecraft.util.Mth;

// https://github.com/CaffeineMC/sodium-fabric/blob/5d364ed5ba63f9067fcf72a078ca310bff4db3e9/src/main/java/me/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuilder.java
public class BatchExecutor implements TaskEngine {
	private static final Logger LOGGER = LogManager.getLogger("BatchExecutor");

	private final AtomicBoolean running = new AtomicBoolean(false);
	private final WaitGroup wg = new WaitGroup();

	private final Deque<Runnable> jobQueue = new ConcurrentLinkedDeque<>();
	private final List<Thread> threads = new ArrayList<>();

	private final Object jobNotifier = new Object();

	private final int threadCount;

	public BatchExecutor() {
		threadCount = getOptimalThreadCount();
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

			Thread thread = new Thread(new WorkerRunnable(), "Engine Executor " + i);
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
		while ((job = getNextTask(false)) != null) {
			processTask(job);
		}

		// and wait for any stragglers.
		try {
			this.wg.await();
		} catch (InterruptedException ignored) {
		}
	}

	@Nullable
	private Runnable getNextTask(boolean block) {
		Runnable job = this.jobQueue.poll();

		if (job == null && block) {
			synchronized (BatchExecutor.this.jobNotifier) {
				try {
					BatchExecutor.this.jobNotifier.wait();
				} catch (InterruptedException ignored) {
				}
			}
		}

		return job;
	}

	private void processTask(Runnable job) {
		try {
			job.run();
		} finally {
			BatchExecutor.this.wg.done();
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

		private final AtomicBoolean running = BatchExecutor.this.running;

		@Override
		public void run() {
			// Run until the chunk builder shuts down
			while (this.running.get()) {
				Runnable job = BatchExecutor.this.getNextTask(true);

				if (job == null) {
					continue;
				}

				processTask(job);
			}
		}

	}
}
