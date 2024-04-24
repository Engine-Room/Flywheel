package com.jozufozu.flywheel.lib.task;

import java.util.concurrent.atomic.AtomicInteger;

public class Synchronizer implements Runnable {
	private final AtomicInteger countDown;
	private final Runnable onCompletion;

	public Synchronizer(int countDown, Runnable onCompletion) {
		this.countDown = new AtomicInteger(countDown);
		this.onCompletion = onCompletion;
	}

	public void decrementAndEventuallyRun() {
		if (countDown.decrementAndGet() == 0) {
			onCompletion.run();
		}
	}

	@Override
	public void run() {
		decrementAndEventuallyRun();
	}
}
