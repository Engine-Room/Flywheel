package com.jozufozu.flywheel.backend.instancing;

import java.util.concurrent.Executor;

public interface TaskExecutor extends Executor {
	/**
	 * Wait for all running tasks to finish.
	 */
	void syncPoint();

	int getThreadCount();
}
