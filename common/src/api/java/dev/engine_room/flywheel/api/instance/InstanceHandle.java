package dev.engine_room.flywheel.api.instance;

import dev.engine_room.flywheel.api.BackendImplemented;

@BackendImplemented
public interface InstanceHandle {
	void setChanged();

	void setDeleted();
}
