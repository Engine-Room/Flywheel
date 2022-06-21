package com.jozufozu.flywheel.backend.instancing.batching;

import java.util.concurrent.atomic.AtomicInteger;

// https://stackoverflow.com/questions/29655531
public class WaitGroup {

	private final AtomicInteger counter = new AtomicInteger(0);

	public synchronized void add(int i) {
		if (i == 0) {
			return;
		}

		if (i == 1) {
			this.counter.incrementAndGet();
		} else {
			this.counter.addAndGet(i);
		}
	}

	public synchronized void done() {
		if (this.counter.decrementAndGet() == 0) {
			this.notifyAll();
		}
	}

	public synchronized void await() throws InterruptedException {
		while (this.counter.get() > 0) {
			this.wait();
		}
	}

}
