package com.jozufozu.flywheel.impl.task;

import org.apache.commons.lang3.concurrent.AtomicSafeInitializer;
import org.apache.commons.lang3.concurrent.ConcurrentUtils;

public final class FlwTaskExecutor {
	// TODO: system property to use SerialTaskExecutor
	private static final Initializer INITIALIZER = new Initializer();

	private FlwTaskExecutor() {
	}

	/**
	 * Get a thread pool for running Flywheel related work in parallel.
	 * @return A global Flywheel thread pool.
	 */
	public static ParallelTaskExecutor get() {
		return ConcurrentUtils.initializeUnchecked(INITIALIZER);
	}

	private static class Initializer extends AtomicSafeInitializer<ParallelTaskExecutor> {
		@Override
		protected ParallelTaskExecutor initialize() {
			ParallelTaskExecutor executor = new ParallelTaskExecutor("Flywheel");
			executor.startWorkers();
			return executor;
		}
	}
}
