package com.jozufozu.flywheel.lib.task;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.mojang.logging.LogUtils;

public class WaitGroup {
	private static final Logger LOGGER = LogUtils.getLogger();

	private final AtomicInteger counter = new AtomicInteger(0);

	public void add() {
		add(1);
	}

	public void add(int i) {
		Preconditions.checkArgument(i >= 0, "Cannot add a negative number of tasks to a WaitGroup!");
		if (i == 0) {
			return;
		}

		counter.addAndGet(i);
	}

	public void done() {
		if (counter.decrementAndGet() < 0) {
			throw new IllegalStateException("WaitGroup counter is negative!");
		}
	}

	/**
	 * Spins for up to the given number of nanoseconds before returning.
	 *
	 * @param nsTimeout How long to wait for the counter to reach 0.
	 * @return {@code true} if the counter reached 0, {@code false} if the timeout was reached.
	 */
	public boolean await(int nsTimeout) {
		long startTime = System.nanoTime();
		while (counter.get() > 0) {
			if (System.nanoTime() - startTime > nsTimeout) {
				return false;
			}
			// spin in place to avoid sleeping the main thread
			Thread.onSpinWait();
		}
		return true;
	}

	public void _reset() {
		counter.set(0);
	}
}
