package com.jozufozu.flywheel.backend.instancing;

import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;

public abstract class InstanceData {

	Instancer<?> owner;

	boolean dirty;
	boolean removed;

	public abstract void write(VecBuffer buf);

	public void markDirty() {
		owner.markDirty(this);
	}

	public void delete() {
		owner.markRemoval(this);
	}

}
