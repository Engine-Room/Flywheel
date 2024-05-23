package dev.engine_room.flywheel.api.instance;

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
