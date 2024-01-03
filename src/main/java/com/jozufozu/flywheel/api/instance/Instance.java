package com.jozufozu.flywheel.api.instance;

public interface Instance {
	InstanceType<?> type();

	InstanceHandle handle();

	default void delete() {
		handle().setDeleted();
	}

	default void setChanged() {
		handle().setChanged();
	}
}
