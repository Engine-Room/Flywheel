package dev.engine_room.flywheel.api.visual;

/**
 * A general interface providing information about any type of thing that could use Flywheel's visualized rendering.
 *
 * @see DynamicVisual
 * @see TickableVisual
 * @see LitVisual
 */
public interface Visual {
	/**
	 * Update instances here.
	 *
	 * <p>Good for when instances don't change very often and when animations are GPU based.
	 *
	 * <br>If your animations are complex or more CPU driven, see {@link DynamicVisual} or {@link TickableVisual}.</p>
	 */
	void update(float partialTick);

	/**
	 * Free any acquired resources.
	 */
	void delete();
}
