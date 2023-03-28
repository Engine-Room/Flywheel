package com.jozufozu.flywheel.backend.instancing;

public class SerialTaskExecutor implements TaskExecutor {
	public static final SerialTaskExecutor INSTANCE = new SerialTaskExecutor();

	private SerialTaskExecutor() {
	}

	@Override
	public void execute(Runnable task) {
		task.run();
	}

	@Override
	public void syncPoint() {
		// noop
	}

	@Override
	public int getThreadCount() {
		return 1;
	}
}
