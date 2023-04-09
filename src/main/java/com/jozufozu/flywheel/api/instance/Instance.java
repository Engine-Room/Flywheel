package com.jozufozu.flywheel.api.instance;

public interface Instance {
	InstanceType<?> type();

	@Deprecated
	Instance copy(InstanceHandle handle);
}
