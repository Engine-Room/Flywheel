package com.jozufozu.flywheel.backend.util;

public abstract class ReferenceCounted {
	private int referenceCount = 0;
	private boolean isDeleted = false;

	public int referenceCount() {
		return referenceCount;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public void acquire() {
		if (isDeleted) {
			throw new IllegalStateException("Tried to acquire deleted instance of '" + getClass().getName()  + "'!");
		}

		referenceCount++;
	}

	public void release() {
		if (isDeleted) {
			throw new IllegalStateException("Tried to release deleted instance of '" + getClass().getName()  + "'!");
		}

		int newCount = --referenceCount;
		if (newCount == 0) {
			isDeleted = true;
			delete();
		} else if (newCount < 0) {
			throw new IllegalStateException("Tried to delete instance of '" + getClass().getName()  + "' more times than it was acquired!");
		}
	}

	protected abstract void delete();
}
