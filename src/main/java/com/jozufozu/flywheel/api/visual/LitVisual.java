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
	 * <br>
	 * The implementation is free to parallelize calls to this method, as well as call into
	 * {@link DynamicVisual#beginFrame} simultaneously. It is safe to query/update light here,
	 * but you must ensure proper synchronization if you want to mutate anything outside this
	 * visual or anything that is also mutated by {@link DynamicVisual#beginFrame}.
	 */
	void updateLight();

	/**
	 * Collect the sections that this visual is contained in.
	 * <br>
	 * This method is called upon visual creation, and the frame after {@link Notifier#notifySectionsChanged} is called.
	 *
	 * @param consumer The consumer to provide the sections to.
	 * @see SectionPos#asLong
	 */
	void collectLightSections(LongConsumer consumer);

	/**
	 * Set the notifier object.
	 * <br>
	 * This method is only called once, upon visual creation,
	 * after {@link #init} and before {@link #collectLightSections}.
	 *
	 * @param notifier The notifier.
	 */
	void initLightSectionNotifier(Notifier notifier);

	/**
	 * A notifier object that can be used to indicate to the impl
	 * that the sections a visual is contained in have changed.
	 */
	interface Notifier {
		/**
		 * Invoke this to indicate to the impl that your visual has moved to a different set of sections.
		 * <br>
		 * The next frame, the impl will call {@link LitVisual#collectLightSections} again.
		 */
		void notifySectionsChanged();
	}
}
