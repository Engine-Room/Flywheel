package com.jozufozu.flywheel.backend.task;

public class FlwTaskExecutor {
	private static ParallelTaskExecutor executor;

	/**
	 * Get a thread pool for running Flywheel related work in parallel.
	 * @return A global Flywheel thread pool.
	 */
	public static ParallelTaskExecutor get() {
		if (executor == null) {
			executor = new ParallelTaskExecutor("Flywheel");
			executor.startWorkers();
		}

		return executor;
	}
}
