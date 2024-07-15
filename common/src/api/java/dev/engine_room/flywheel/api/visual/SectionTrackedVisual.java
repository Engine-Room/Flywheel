package dev.engine_room.flywheel.api.visual;

import org.jetbrains.annotations.ApiStatus;

import it.unimi.dsi.fastutil.longs.LongSet;

public sealed interface SectionTrackedVisual extends Visual permits ShaderLightVisual, LightUpdatedVisual {
	/**
	 * Set the section property object.
	 *
	 * <p>This method is only called once, upon visual creation.
	 * <br>If the property is assigned to in this method, the
	 * visual will immediately be tracked in the given sections.
	 *
	 * @param property The property.
	 */
	void setSectionCollector(SectionCollector property);

	@ApiStatus.NonExtendable
	interface SectionCollector {
		/**
		 * Assign the set of sections this visual wants to track itself in.
		 */
		void sections(LongSet sections);
	}
}
