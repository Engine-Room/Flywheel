package com.jozufozu.flywheel.api.visual;

import java.util.function.LongConsumer;

import net.minecraft.core.SectionPos;

/**
 * A non-moving visual that listens to light updates.
 * <br>
 * If your visual moves around in the world at all, you should use {@link TickableVisual} or {@link DynamicVisual},
 * and poll for light yourself rather than listening for updates.
 */
public interface LitVisual extends Visual {
	/**
	 * Called when a section this visual is contained in receives a light update.
	 * <br>
	 * Even if multiple sections are updated at the same time, this method will only be called once.
	 */
	void updateLight();

	/**
	 * Collect the sections that this visual is contained in.
	 * <br>
	 * This method is only called upon visual creation.
	 *
	 * @param consumer The consumer to provide the sections to.
	 * @see SectionPos#asLong
	 */
	void collectLightSections(LongConsumer consumer);
}
