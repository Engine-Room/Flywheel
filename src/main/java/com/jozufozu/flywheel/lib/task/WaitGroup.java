package com.jozufozu.flywheel.lib.task;

import java.util.concurrent.atomic.AtomicInteger;

// https://stackoverflow.com/questions/29655531
public class WaitGroup {
	private final AtomicInteger counter = new AtomicInteger(0);

	public void add() {
		add(1);
	}

	public void add(int i) {
		if (i == 0) {
			return;
		}

		counter.addAndGet(i);
	}

	public void done() {
		var result = counter.decrementAndGet();
		if (result == 0) {
			synchronized (this) {
				this.notifyAll();
			}
		} else if (result < 0) {
			throw new IllegalStateException("WaitGroup counter is negative!");
		}
	}

	public void await() {
		try {
			awaitInternal();
		} catch (InterruptedException ignored) {
			// noop
		}
	}

	private void awaitInternal() throws InterruptedException {
		//		var start = System.nanoTime();
		while (counter.get() > 0) {
			// spin in place to avoid sleeping the main thread
			//			synchronized (this) {
			//				this.wait(timeoutMs);
			//			}
		}
		//		var end = System.nanoTime();
		//		var elapsed = end - start;
		//
		//		if (elapsed > 1000000) {
		//			Flywheel.LOGGER.info("Waited " + StringUtil.formatTime(elapsed));
		//		}
	}

	public void _reset() {
		counter.set(0);
	}
}
