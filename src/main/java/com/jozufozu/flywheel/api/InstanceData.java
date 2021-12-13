package com.jozufozu.flywheel.api;

public abstract class InstanceData {

	private Instancer<?> owner;

	private boolean dirty;
	private boolean removed;

	public final void markDirty() {
		dirty = true;
		owner.notifyDirty();
	}

	public final void delete() {
		removed = true;
		owner.notifyRemoval();
	}

	public final boolean checkDirtyAndClear() {
		if (dirty) {
			dirty = false;
			return true;
		} else {
			return false;
		}
	}

	public final boolean isRemoved() {
		return removed;
	}

	public Instancer<?> getOwner() {
		return owner;
	}

	public InstanceData setOwner(Instancer<?> owner) {
		this.owner = owner;
		return this;
	}
}
