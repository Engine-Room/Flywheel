package com.jozufozu.flywheel.api.instancer;

import com.jozufozu.flywheel.api.struct.StructType;

public interface InstancePart {
	StructType<?> type();

	@Deprecated
	InstancePart copy(Handle handle);
}
