package dev.engine_room.flywheel.api.visual;

import org.jetbrains.annotations.ApiStatus;

import it.unimi.dsi.fastutil.longs.LongSet;

public sealed interface SectionTrackedVisual extends Visual permits SmoothLitVisual, LitVisual {
	/**
	 * Set the section property object.
	 *
	 * <p>This method is only called once, upon visual creation.
	 * <br>If the property is assigned to in this method, the
	 * visual will immediately be tracked in the given sections.
	 *
	 * @param property The property.
	 */
	void setSectionProperty(SectionProperty property);

	@ApiStatus.NonExtendable
	interface SectionProperty {
		/**
		 * Assign the set of sections this visual wants to track itself in.
		 */
		void lightSections(LongSet sections);
	}
}
