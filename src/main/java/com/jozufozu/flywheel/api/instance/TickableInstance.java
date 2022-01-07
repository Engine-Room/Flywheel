package com.jozufozu.flywheel.api.instance;

import com.jozufozu.flywheel.api.InstanceData;
import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstance;

/**
 * An interface giving {@link BlockEntityInstance}s a hook to have a function called at
 * the end of every tick. By implementing {@link TickableInstance}, a {@link BlockEntityInstance}
 * can update frequently, but not every frame.
 * <br> There are a few cases in which this should be considered over {@link DynamicInstance}:
 * <ul>
 *     <li>
 *         You'd like to change something about the instance every now and then.
 *         eg. adding or removing parts, snapping to a different rotation, etc.
 *     </li>
 *     <li>
 *         Your BlockEntity does animate, but the animation doesn't have
 *         to be smooth, in which case this could be an optimization.
 *     </li>
 * </ul>
 */
public interface TickableInstance extends Instance {

	/**
	 * Called every tick, and after initialization.
	 * <br>
	 * <em>DISPATCHED IN PARALLEL</em>, don't attempt to mutate anything outside of this instance.
	 * <br>
	 * {@link Instancer}/{@link InstanceData} creation/acquisition is safe here.
	 */
	void tick();

	/**
	 * As a further optimization, tickable instances that are far away are ticked less often.
	 * This behavior can be disabled by returning false.
	 *
	 * <br> You might want to opt out of this if you want your animations to remain smooth
	 * even when far away from the camera. It is recommended to keep this as is, however.
	 *
	 * @return {@code true} if your instance should be slow ticked.
	 */
	default boolean decreaseTickRateWithDistance() {
		return true;
	}
}
