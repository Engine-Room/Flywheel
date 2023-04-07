package com.jozufozu.flywheel.backend.engine;

import com.jozufozu.flywheel.api.struct.Handle;

public class HandleImpl implements Handle {
	private final AbstractInstancer<?> instancer;
	private int index;

	public HandleImpl(AbstractInstancer<?> instancer, int index) {
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

	public void setIndex(int i) {
		index = i;
	}

	public void clear() {
		index = -1;
	}
}
