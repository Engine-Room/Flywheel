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
	public final InstanceType<?> type() {
		return type;
	}

	@Override
	public final InstanceHandle handle() {
		return handle;
	}

	@Override
	public final void setChanged() {
		// Override to mark final.
		handle.setChanged();
	}

	@Override
	public final void delete() {
		// Override to mark final.
		handle.setDeleted();
	}
}
