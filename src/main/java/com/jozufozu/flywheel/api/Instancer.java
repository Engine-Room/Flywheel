package com.jozufozu.flywheel.api;

/**
 * An instancer is how you interact with an instanced model.
 * <p>
 *     Instanced models can have many copies, and on most systems it's very fast to draw all of the copies at once.
 *     There is no limit to how many copies an instanced model can have.
 *     Each copy is represented by an InstanceData object.
 * </p>
 * <p>
 *     When you call {@link #createInstance()} you are given an InstanceData object that you can manipulate however
 *     you want. The changes you make to the InstanceData object are automatically made visible, and persistent.
 *     Changing the position of your InstanceData object every frame means that that copy of the model will be in a
 *     different position in the world each frame. Setting the position of your InstanceData once and not touching it
 *     again means that your model will be in the same position in the world every frame. This persistence is useful
 *     because it means the properties of your model don't have to be re-evaluated every frame.
 * </p>
 *
 * @param <D> the data that represents a copy of the instanced model.
 */
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

	/**
	 * Notify the Instancer that some of its data needs updating.
	 *
	 * <p>
	 *     This might be ignored, depending on the implementation. For the GPUInstancer, this triggers a scan of all
	 *     instances.
	 * </p>
	 */
	void notifyDirty();

	/**
	 * Notify the Instances that some of its data should be removed.
	 *
	 * <p>
	 *     By the time the next frame is drawn, the instanceData passed will no longer be considered for rendering.
	 * </p>
	 */
	void notifyRemoval();

	/**
	 * Populate arr with new instances of this model.
	 * @param arr An array to fill.
	 */
	default void createInstances(D[] arr) {
		for (int i = 0; i < arr.length; i++) {
			arr[i] = createInstance();
		}
	}
}
