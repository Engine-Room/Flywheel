package dev.engine_room.flywheel.api.visual;

import org.jetbrains.annotations.ApiStatus;

import it.unimi.dsi.fastutil.longs.LongSet;

/**
 * An interface allowing visuals to request light data on the GPU for a set of sections.
 *
 * <p> Sections passed into {@link SectionProperty#lightSections} will have their light data handed to the
 * backend and queryable by {@code flw_light*} functions in shaders.
 * <br>
 * Note that the queryable light data is shared across all visuals, so even if one specific visual does not
 * request a given section, the data will be available if another visual does.
 */
public interface SmoothLitVisual extends Visual {
	/**
	 * Set the section property object.
	 *
	 * <p>This method is only called once, upon visual creation.
	 *
	 * @param property The property.
	 */
	void setSectionProperty(SectionProperty property);

	@ApiStatus.NonExtendable
	interface SectionProperty {
		/**
		 * Assign the set of sections this visual wants to have light data for.
		 */
		void lightSections(LongSet sections);
	}
}
