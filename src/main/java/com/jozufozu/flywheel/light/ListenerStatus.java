package com.jozufozu.flywheel.light;

public enum ListenerStatus {
	OKAY,
	REMOVE,
	UPDATE,
	;

	public boolean isOk() {
		return this == OKAY;
	}

	public boolean shouldRemove() {
		return this == REMOVE;
	}
}
