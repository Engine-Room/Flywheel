package dev.engine_room.flywheel.impl.task;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;

/** Signals registered workers without acquiring a monitor held by a worker. */
public class ThreadGroupNotifier {
	private final CopyOnWriteArrayList<Thread> threads = new CopyOnWriteArrayList<>();

	/** Register before starting the worker; unregister only after it has joined. */
	public void register(Thread thread) {
		threads.add(thread);
	}

	public void clear() {
		threads.clear();
	}

	public void awaitNotification(BooleanSupplier shouldWait) {
		while (!Thread.interrupted() && shouldWait.getAsBoolean()) {
			// unpark retains a permit if a notification arrives between the condition
			// check and park. Recheck after spurious wakeups; no periodic polling.
			LockSupport.park(this);
		}
	}

	public void postNotification() {
		for (Thread thread : threads) {
			LockSupport.unpark(thread);
		}
	}
}
