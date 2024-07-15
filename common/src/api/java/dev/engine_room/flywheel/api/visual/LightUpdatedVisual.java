package dev.engine_room.flywheel.api.visual;

/**
 * A visual that listens to light updates.
 *
 * <p>If your visual moves around in the level at all, you should use {@link TickableVisual} or {@link DynamicVisual},
 * and poll for light yourself along with listening for updates. When your visual moves to a different section, call
 * {@link SectionCollector#sections}.</p>
 */
public non-sealed interface LightUpdatedVisual extends SectionTrackedVisual {
	/**
	 * Called when a section this visual is contained in receives a light update.
	 *
	 * <p>Even if multiple sections are updated at the same time, this method will only be called once.</p>
	 *
	 * <p>The implementation is free to parallelize calls to this method, as well as execute the plan
	 * returned by {@link DynamicVisual#planFrame} simultaneously. It is safe to query/update light here,
	 * but you must ensure proper synchronization if you want to mutate anything outside this visual or
	 * anything that is also mutated within {@link DynamicVisual#planFrame}.</p>
	 *
	 * <p>This method not is invoked automatically after visual creation.</p>
	 */
	void updateLight(float partialTick);
}
