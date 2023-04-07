package com.jozufozu.flywheel.api.struct;

public interface InstancePart {
	StructType<?> type();

	@Deprecated
	InstancePart copy(Handle handle);
}
