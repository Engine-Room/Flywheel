package dev.engine_room.flywheel.api.visual;

/**
 * A marker interface allowing visuals to request light data on the GPU for a set of sections.
 *
 * <p> Sections passed into {@link SectionCollector#sections} will have their light data handed to the
 * backend and queryable by {@code flw_light*} functions in shaders.
 * <br>
 * Note that the queryable light data is shared across all visuals, so even if one specific visual does not
 * request a given section, the data will be available if another visual does.
 */
public non-sealed interface ShaderLightVisual extends SectionTrackedVisual {

}
