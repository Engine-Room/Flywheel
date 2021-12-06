package com.jozufozu.flywheel.backend.instancing;

public abstract class InstanceData {

	Instancer<?> owner;

	boolean dirty;
	boolean removed;

	public void markDirty() {
		owner.markDirty(this);
	}

	public void delete() {
		owner.markRemoval(this);
	}

}
