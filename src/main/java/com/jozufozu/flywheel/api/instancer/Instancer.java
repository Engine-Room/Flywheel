package com.jozufozu.flywheel.api.instancer;

import com.jozufozu.flywheel.api.struct.InstancePart;

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
 * @param <P> the data that represents a copy of the instanced model.
 */
public interface Instancer<P extends InstancePart> {
	/**
	 * @return a handle to a new copy of this model.
	 */
	P createInstance();

	/**
	 * Populate arr with new instances of this model.
	 *
	 * @param arr An array to fill.
	 */
	default void createInstances(P[] arr) {
		for (int i = 0; i < arr.length; i++) {
			arr[i] = createInstance();
		}
	}
}
