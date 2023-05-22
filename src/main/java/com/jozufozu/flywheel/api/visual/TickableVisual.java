package com.jozufozu.flywheel.api.visual;

import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.Instancer;

/**
 * An interface giving {@link Visual}s a hook to have a function called at
 * the end of every tick. By implementing {@link TickableVisual}, an {@link Visual}
 * can update frequently, but not every frame.
 * <br> There are a few cases in which this should be considered over {@link DynamicVisual}:
 * <ul>
 *     <li>
 *         You'd like to change something about the visual every now and then.
 *         eg. adding or removing instances, snapping to a different rotation, etc.
 *     </li>
 *     <li>
 *         Your BlockEntity does animate, but the animation doesn't have
 *         to be smooth, in which case this could be an optimization.
 *     </li>
 * </ul>
 */
public interface TickableVisual extends Visual {
	/**
	 * Called every tick.
	 * <p>
	 * <b>DISPATCHED IN PARALLEL</b>. Ensure proper synchronization if you need to mutate anything outside this visual.
	 * <p>
	 * {@link Instancer}/{@link Instance} creation/acquisition is safe here.
	 */
	void tick(VisualTickContext c);
}
