package com.jozufozu.flywheel.fabric.event;

public class EventContext {
	protected boolean isCanceled = false;

	public boolean isCancelable() {
		return false;
	}

	public boolean isCanceled() {
		return isCanceled;
	}

	public void setCanceled(boolean canceled) {
		if (!isCancelable()) {
			throw new UnsupportedOperationException("Cannot cancel event of class " + getClass().getName());
		}
		isCanceled = canceled;
	}

	public interface Listener<C extends EventContext> {
		void handleEvent(C context);
	}
}
