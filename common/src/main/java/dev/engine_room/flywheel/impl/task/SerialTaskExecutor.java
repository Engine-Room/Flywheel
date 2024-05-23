package dev.engine_room.flywheel.impl.task;

import java.util.function.BooleanSupplier;

import dev.engine_room.flywheel.api.task.TaskExecutor;

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
	public boolean syncUntil(BooleanSupplier cond) {
		return cond.getAsBoolean();
	}

	@Override
	public boolean syncWhile(BooleanSupplier cond) {
		return !cond.getAsBoolean();
	}

	@Override
	public int threadCount() {
		return 1;
	}

	@Override
	public boolean isMainThread() {
		return true;
	}
}
