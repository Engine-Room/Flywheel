package com.jozufozu.flywheel.api.instance;

import com.jozufozu.flywheel.api.InstanceData;
import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstance;

/**
 * An interface giving {@link BlockEntityInstance}s a hook to have a function called at
 * the start of a frame. By implementing {@link DynamicInstance}, a {@link BlockEntityInstance}
 * can animate its models in ways that could not be easily achieved by shader attribute
 * parameterization.
 *
 * <br><br> If your goal is offloading work to shaders, but you're unsure exactly how you need
 * to parameterize the instances, you're encouraged to implement this for prototyping.
 */
public interface DynamicInstance extends Instance {
	/**
	 * Called every frame, and after initialization.
	 * <br>
	 * <em>DISPATCHED IN PARALLEL</em>, don't attempt to mutate anything outside this instance.
	 * <br>
	 * {@link Instancer}/{@link InstanceData} creation/acquisition is safe here.
	 */
	void beginFrame();

	/**
	 * As a further optimization, dynamic instances that are far away are ticked less often.
	 * This behavior can be disabled by returning false.
	 *
	 * <br> You might want to opt out of this if you want your animations to remain smooth
	 * even when far away from the camera. It is recommended to keep this as is, however.
	 *
	 * @return {@code true} if your instance should be slow ticked.
	 */
	default boolean decreaseFramerateWithDistance() {
		return true;
	}
}
