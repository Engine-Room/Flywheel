package com.jozufozu.flywheel.backend.engine;

import com.jozufozu.flywheel.api.instance.InstanceHandle;

public class InstanceHandleImpl implements InstanceHandle {
	public final AbstractInstancer<?> instancer;
	public int index;

	public InstanceHandleImpl(AbstractInstancer<?> instancer, int index) {
		this.instancer = instancer;
		this.index = index;
	}

	@Override
	public void setChanged() {
		instancer.notifyDirty(index);
	}

	@Override
	public void setDeleted() {
		instancer.notifyRemoval(index);
	}

	public void clear() {
		index = -1;
	}
}
