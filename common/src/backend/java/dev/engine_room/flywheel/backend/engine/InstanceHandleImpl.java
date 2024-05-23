package dev.engine_room.flywheel.backend.engine;

import dev.engine_room.flywheel.api.instance.InstanceHandle;

public class InstanceHandleImpl implements InstanceHandle {
	public AbstractInstancer<?> instancer;
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
		// invalidate ourselves
		clear();
	}

	public void clear() {
		index = -1;
	}
}
