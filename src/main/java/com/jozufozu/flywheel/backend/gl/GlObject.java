package com.jozufozu.flywheel.backend.gl;

// Utility class for safely dealing with gl object handles.
public abstract class GlObject {
	private static final int INVALID_HANDLE = Integer.MIN_VALUE;

	private int handle = INVALID_HANDLE;

	protected final void setHandle(int handle) {
		this.handle = handle;
	}

	public final int handle() {
		this.checkHandle();

		return this.handle;
	}

	protected final void checkHandle() {
		if (this.isInvalid()) {
			String descriptor = getDescriptor();
			String message = (descriptor == null ? "" : (descriptor + " ")) + "handle is not valid.";
			throw new IllegalStateException(message);
		}
	}

	protected final boolean isInvalid() {
		return this.handle == INVALID_HANDLE;
	}

	protected final void invalidateHandle() {
		this.handle = INVALID_HANDLE;
	}

	public void delete() {
		if (isInvalid()) {
			String descriptor = getDescriptor();
			String message = (descriptor == null ? "" : (descriptor + " ")) + "handle already deleted.";
			throw new IllegalStateException(message);
		}

		deleteInternal(handle);
		invalidateHandle();
	}

	protected abstract void deleteInternal(int handle);

	protected String getDescriptor() {
		return "";
	}
}
