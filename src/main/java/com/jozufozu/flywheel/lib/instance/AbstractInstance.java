package com.jozufozu.flywheel.lib.instance;

import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceHandle;
import com.jozufozu.flywheel.api.instance.InstanceType;

public abstract class AbstractInstance implements Instance {
	protected final InstanceType<?> type;
	protected final InstanceHandle handle;

	protected AbstractInstance(InstanceType<?> type, InstanceHandle handle) {
		this.type = type;
		this.handle = handle;
	}

	@Override
	public InstanceType<?> type() {
		return type;
	}

	public final void setChanged() {
		handle.setChanged();
	}

	public final void delete() {
		handle.setDeleted();
	}
}
