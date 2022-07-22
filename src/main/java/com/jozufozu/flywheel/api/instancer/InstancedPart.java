package com.jozufozu.flywheel.api.instancer;

import com.jozufozu.flywheel.api.struct.StructType;

public abstract class InstancedPart {

	public final StructType<?> type;
	private Instancer<?> owner;

	private boolean dirty;
	private boolean removed;

	protected InstancedPart(StructType<?> type) {
		this.type = type;
	}

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

	public void setOwner(Instancer<?> owner) {
		this.owner = owner;
	}

    public abstract InstancedPart copy();
}
