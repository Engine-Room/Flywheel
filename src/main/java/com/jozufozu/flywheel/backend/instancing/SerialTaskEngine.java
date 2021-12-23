package com.jozufozu.flywheel.backend.instancing;

import org.jetbrains.annotations.NotNull;

public class SerialTaskEngine implements TaskEngine {

	public static final SerialTaskEngine INSTANCE = new SerialTaskEngine();

	private SerialTaskEngine() {
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
