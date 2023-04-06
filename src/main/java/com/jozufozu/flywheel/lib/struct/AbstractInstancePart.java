package com.jozufozu.flywheel.lib.struct;

import com.jozufozu.flywheel.api.instancer.Handle;
import com.jozufozu.flywheel.api.instancer.InstancePart;
import com.jozufozu.flywheel.api.struct.StructType;

public abstract class AbstractInstancePart implements InstancePart {

	public final StructType<?> type;
	protected final Handle handle;

	protected AbstractInstancePart(StructType<?> type, Handle handle) {
		this.type = type;
		this.handle = handle;
	}

	@Override
	public StructType<?> type() {
		return type;
	}

	public final void setChanged() {
		handle.setChanged();
	}

	public final void delete() {
		handle.setDeleted();
	}
}
