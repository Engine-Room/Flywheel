package com.jozufozu.flywheel.api.instance;

import com.jozufozu.flywheel.api.BackendImplemented;

@BackendImplemented
public interface InstanceHandle {
	void setChanged();

	void setDeleted();
}
