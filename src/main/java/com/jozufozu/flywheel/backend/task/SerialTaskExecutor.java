package com.jozufozu.flywheel.backend.task;

import com.jozufozu.flywheel.api.task.TaskExecutor;

public class SerialTaskExecutor implements TaskExecutor {
	public static final SerialTaskExecutor INSTANCE = new SerialTaskExecutor();

	private SerialTaskExecutor() {
	}

	@Override
	public void execute(Runnable task) {
		task.run();
	}

	@Override
	public void scheduleForMainThread(Runnable runnable) {
		runnable.run();
	}

	@Override
	public void syncPoint() {
	}

	@Override
	public int getThreadCount() {
		return 1;
	}
}
