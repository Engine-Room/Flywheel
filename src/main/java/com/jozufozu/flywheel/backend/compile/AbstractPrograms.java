package com.jozufozu.flywheel.backend.compile;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractPrograms {
	private final AtomicInteger refCount = new AtomicInteger();
	private volatile boolean isDeleted;

	public int refCount() {
		return refCount.get();
	}

	public void acquire() {
		if (isDeleted) {
			throw new IllegalStateException("Tried to acquire deleted instance of '" + getClass().getName()  + "'!");
		}
		refCount.getAndIncrement();
	}

	public void release() {
		int newCount = refCount.decrementAndGet();
		if (newCount == 0) {
			isDeleted = true;
			delete();
		} else if (newCount < 0) {
			throw new IllegalStateException("Tried to delete instance of '" + getClass().getName()  + "' more times than it was acquired!");
		}
	}

	protected abstract void delete();
}
