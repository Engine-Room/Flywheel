package com.jozufozu.flywheel.backend.instancing.batching;

// https://stackoverflow.com/questions/29655531
public class WaitGroup {

	private int jobs = 0;

	public synchronized void add(int i) {
		jobs += i;
	}

	public synchronized void done() {
		if (--jobs == 0) {
			notifyAll();
		}
	}

	public synchronized void await() throws InterruptedException {
		while (jobs > 0) {
			wait();
		}
	}

}
