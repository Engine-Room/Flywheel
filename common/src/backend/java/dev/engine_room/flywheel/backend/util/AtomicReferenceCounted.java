package dev.engine_room.flywheel.backend.util;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class AtomicReferenceCounted {
	private final AtomicInteger referenceCount = new AtomicInteger(0);
	private volatile boolean isDeleted = false;

	public int referenceCount() {
		return referenceCount.get();
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public void acquire() {
		if (isDeleted) {
			throw new IllegalStateException("Tried to acquire deleted instance of '" + getClass().getName()  + "'!");
		}

		referenceCount.getAndIncrement();
	}

	public void release() {
		if (isDeleted) {
			throw new IllegalStateException("Tried to release deleted instance of '" + getClass().getName()  + "'!");
		}

		int newCount = referenceCount.decrementAndGet();
		if (newCount == 0) {
			isDeleted = true;
			_delete();
		} else if (newCount < 0) {
			throw new IllegalStateException("Tried to delete instance of '" + getClass().getName()  + "' more times than it was acquired!");
		}
	}

	protected abstract void _delete();
}
