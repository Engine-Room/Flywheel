package dev.engine_room.flywheel.impl.task;

import java.util.concurrent.atomic.AtomicReference;

import dev.engine_room.flywheel.impl.FlwConfig;
import net.minecraft.util.Mth;

public final class FlwTaskExecutor {
	private static final AtomicLazy INSTANCE = new AtomicLazy();

	private FlwTaskExecutor() {
	}

	/**
	 * Get a thread pool for running Flywheel related work in parallel.
	 * @return A global Flywheel thread pool.
	 */
	public static TaskExecutorImpl get() {
		return INSTANCE.get();
	}

	/**
	 * Returns the "optimal" number of threads to be used for tasks. This will always return at least one thread.
	 */
	private static int getOptimalThreadCount() {
		return Mth.clamp(Math.max(getMaxThreadCount() / 3, getMaxThreadCount() - 6), 1, 10);
	}

	private static int getMaxThreadCount() {
		return Runtime.getRuntime()
				.availableProcessors();
	}

	/**
	 * Copy of apache commons' {@code AtomicSafeInitializer}
	 */
	private static class AtomicLazy {
		private final AtomicReference<AtomicLazy> factory = new AtomicReference<>();

		private final AtomicReference<TaskExecutorImpl> reference = new AtomicReference<>();

		public final TaskExecutorImpl get() {
			TaskExecutorImpl result;

			while ((result = reference.get()) == null) {
				if (factory.compareAndSet(null, this)) {
					reference.set(initialize());
				}
			}

			return result;
		}

		protected TaskExecutorImpl initialize() {
			int threadCount = FlwConfig.INSTANCE
					.workerThreads();

			if (threadCount == 0) {
				return SerialTaskExecutor.INSTANCE;
			} else if (threadCount < 0) {
				threadCount = getOptimalThreadCount();
			} else {
				threadCount = Mth.clamp(threadCount, 1, getMaxThreadCount());
			}

			ParallelTaskExecutor executor = new ParallelTaskExecutor("Flywheel", threadCount);
			executor.startWorkers();
			return executor;
		}
	}
}
