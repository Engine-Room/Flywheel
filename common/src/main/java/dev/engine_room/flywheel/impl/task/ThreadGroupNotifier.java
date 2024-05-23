package dev.engine_room.flywheel.impl.task;

/**
 * Thin wrapper around Java's built-in object synchronization primitives.
 */
public class ThreadGroupNotifier {
	public synchronized void awaitNotification() {
		try {
			this.wait();
		} catch (InterruptedException e) {
			// we don't care if we're interrupted, just continue.
		}
	}

	public synchronized void postNotification() {
		this.notifyAll();
	}
}
