package com.jozufozu.flywheel.impl.task;

import org.apache.commons.lang3.concurrent.AtomicSafeInitializer;
import org.apache.commons.lang3.concurrent.ConcurrentUtils;

import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.jozufozu.flywheel.config.FlwConfig;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.util.Mth;

public final class FlwTaskExecutor {
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


	private static class Initializer extends AtomicSafeInitializer<TaskExecutor> {
		@Override
		protected TaskExecutor initialize() {
			int threadCount = FlwConfig.get()
					.workerThreads();

			if (threadCount == 0) {
				return SerialTaskExecutor.INSTANCE;
			} else if (threadCount < 0) {
				threadCount = getOptimalThreadCount();
			} else {
				threadCount = Mth.clamp(threadCount, 1, getMaxThreadCount());
			}

			ParallelTaskExecutor executor = new ParallelTaskExecutor("Flywheel", threadCount, RenderSystem::isOnRenderThread);
			executor.startWorkers();
			return executor;
		}
	}
}
