package com.jozufozu.flywheel.api.visual;

/**
 * A general interface providing information about any type of thing that could use Flywheel's visualized rendering.
 *
 * @see DynamicVisual
 * @see TickableVisual
 * @see PlannedVisual
 * @see LitVisual
 */
public interface Visual {
	/**
	 * Initialize instances here.
	 */
	void init(float partialTick);

	/**
	 * Update instances here. Good for when instances don't change very often and when animations are GPU based.
	 * <br>
	 * <br> If your animations are complex or more CPU driven, see {@link DynamicVisual} or {@link TickableVisual}.
	 */
	void update(float partialTick);

	/**
	 * When a visual is reset, the visual is deleted and re-created.
	 * <br>
	 * Just before {@link #update)} would be called, {@code shouldReset} is checked.
	 * If this function returns {@code true}, then this visual will be {@link #delete deleted},
	 * and another visual will be constructed to replace it. This allows for more sane resource
	 * acquisition compared to trying to update everything within the lifetime of a visual.
	 *
	 * @return {@code true} if this visual should be discarded and refreshed.
	 */
	boolean shouldReset();

	/**
	 * Free any acquired resources.
	 */
	void delete();
}
