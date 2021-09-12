package com.jozufozu.flywheel.backend.instancing;

public interface Instancer<D extends InstanceData> {
	/**
	 * @return a handle to a new copy of this model.
	 */
	D createInstance();

	/**
	 * Copy a data from another Instancer to this.
	 *
	 * This has the effect of swapping out one model for another.
	 * @param inOther the data associated with a different model.
	 */
	void stealInstance(D inOther);

	void markDirty(InstanceData instanceData);

	void markRemoval(InstanceData instanceData);
}
