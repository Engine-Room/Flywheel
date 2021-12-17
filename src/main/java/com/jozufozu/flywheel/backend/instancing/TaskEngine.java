package com.jozufozu.flywheel.backend.instancing;

import org.jetbrains.annotations.NotNull;

public interface TaskEngine {
	void submit(@NotNull Runnable command);

	/**
	 * Wait for all running jobs to finish.
	 */
	void syncPoint();
}
