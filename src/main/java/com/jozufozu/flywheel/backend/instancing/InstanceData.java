package com.jozufozu.flywheel.backend.instancing;

import com.jozufozu.flywheel.backend.gl.buffer.MappedBuffer;

public abstract class InstanceData {

	Instancer<?> owner;

	boolean dirty;
	boolean removed;

	protected InstanceData(Instancer<?> owner) {
		this.owner = owner;
	}

	public abstract void write(MappedBuffer buf);

	public void markDirty() {
		owner.markDirty(this);
	}

	public void delete() {
		owner.markRemoval(this);
	}

}
