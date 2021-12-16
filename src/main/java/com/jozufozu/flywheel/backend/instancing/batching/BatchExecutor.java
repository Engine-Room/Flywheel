package com.jozufozu.flywheel.backend.instancing.batching;

import java.util.concurrent.Executor;

import org.jetbrains.annotations.NotNull;

public class BatchExecutor implements Executor {
	private final Executor internal;
	private final WaitGroup wg;

	public BatchExecutor(Executor internal) {
		this.internal = internal;

		wg = new WaitGroup();
	}

	@Override
	public void execute(@NotNull Runnable command) {
		wg.add(1);
		internal.execute(() -> {
			// wrapper function to decrement the wait group
			try {
				command.run();
			} catch (Exception ignored) {
			} finally {
				wg.done();
			}
		});
	}

	public void await() throws InterruptedException {
		wg.await();
	}
}
