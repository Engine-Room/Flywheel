package dev.engine_room.flywheel.impl.task;

import org.apache.commons.lang3.concurrent.AtomicSafeInitializer;
import org.apache.commons.lang3.concurrent.ConcurrentUtils;

import dev.engine_room.flywheel.impl.FlwConfig;
import net.minecraft.util.Mth;

public final class FlwTaskExecutor {
	private static final Initializer INITIALIZER = new Initializer();

	private FlwTaskExecutor() {
	}

	/**
	 * Get a thread pool for running Flywheel related work in parallel.
	 * @return A global Flywheel thread pool.
	 */
	public static TaskExecutorImpl get() {
		return ConcurrentUtils.initializeUnchecked(INITIALIZER);
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

	private static class Initializer extends AtomicSafeInitializer<TaskExecutorImpl> {
		@Override
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
