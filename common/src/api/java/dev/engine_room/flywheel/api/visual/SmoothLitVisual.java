package dev.engine_room.flywheel.api.visual;

import org.jetbrains.annotations.ApiStatus;

import it.unimi.dsi.fastutil.longs.LongSet;

public interface SmoothLitVisual extends Visual {
	/**
	 * Set the section property object.
	 *
	 * <p>This method is only called once, upon visual creation,
	 *
	 * @param property The property.
	 */
	void setSectionProperty(SectionProperty property);

	@ApiStatus.NonExtendable
	interface SectionProperty {
		/**
		 * Invoke this to indicate to the impl that your visual has moved to a different set of sections.
		 */
		void lightSections(LongSet sections);
	}
}
