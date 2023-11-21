package com.jozufozu.flywheel.impl.task;

import org.apache.commons.lang3.concurrent.AtomicSafeInitializer;
import org.apache.commons.lang3.concurrent.ConcurrentUtils;

import com.jozufozu.flywheel.api.task.TaskExecutor;

public final class FlwTaskExecutor {
	public static final boolean USE_SERIAL_EXECUTOR = System.getProperty("flw.useSerialExecutor") != null;

	private static final Initializer INITIALIZER = new Initializer();

	private FlwTaskExecutor() {
	}

	/**
	 * Get a thread pool for running Flywheel related work in parallel.
	 * @return A global Flywheel thread pool.
	 */
	public static TaskExecutor get() {
		return ConcurrentUtils.initializeUnchecked(INITIALIZER);
	}

	private static class Initializer extends AtomicSafeInitializer<TaskExecutor> {
		@Override
		protected TaskExecutor initialize() {
			if (USE_SERIAL_EXECUTOR) {
				return SerialTaskExecutor.INSTANCE;
			}

			ParallelTaskExecutor executor = new ParallelTaskExecutor("Flywheel");
			executor.startWorkers();
			return executor;
		}
	}
}
