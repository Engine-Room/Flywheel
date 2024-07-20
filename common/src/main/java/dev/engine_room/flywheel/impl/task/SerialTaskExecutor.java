package dev.engine_room.flywheel.impl.task;

import java.util.function.BooleanSupplier;

public class SerialTaskExecutor implements InternalTaskExecutor {
	public static final SerialTaskExecutor INSTANCE = new SerialTaskExecutor();

	private SerialTaskExecutor() {
	}

	@Override
	public void execute(Runnable runnable) {
		runnable.run();
	}

	@Override
	public void scheduleForMainThread(Runnable runnable) {
		runnable.run();
	}

	@Override
	public boolean isMainThread() {
		return true;
	}

	@Override
	public int threadCount() {
		return 1;
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
	public void syncPoint() {
	}
}
