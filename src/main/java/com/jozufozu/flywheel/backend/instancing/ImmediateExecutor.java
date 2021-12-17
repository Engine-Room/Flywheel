package com.jozufozu.flywheel.backend.instancing;

import org.jetbrains.annotations.NotNull;

public class ImmediateExecutor implements TaskEngine {

	public static final ImmediateExecutor INSTANCE = new ImmediateExecutor();

	private ImmediateExecutor() {
	}

	@Override
	public void submit(@NotNull Runnable command) {
		command.run();
	}

	@Override
	public void syncPoint() {
		// noop
	}
}
