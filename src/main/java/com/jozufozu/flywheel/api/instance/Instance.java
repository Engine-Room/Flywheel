package com.jozufozu.flywheel.api.instance;

import org.joml.FrustumIntersection;

/**
 * A general interface providing information about any type of thing that could use Flywheel's instanced rendering.
 */
public interface Instance {

	/**
	 * Initialize parts here.
	 */
	void init();

	/**
	 * Update instance data here. Good for when data doesn't change very often and when animations are GPU based.
	 *
	 * <br><br> If your animations are complex or more CPU driven, see {@link DynamicInstance} or {@link TickableInstance}.
	 */
	void update();

	/**
	 * When an instance is reset, the instance is deleted and re-created.
	 *
	 * <p>
	 *     Just before {@link #update()} would be called, {@code shouldReset()} is checked.
	 *     If this function returns {@code true}, then this instance will be {@link #delete deleted},
	 *     and another instance will be constructed to replace it. This allows for more sane resource
	 *     acquisition compared to trying to update everything within the lifetime of an instance.
	 * </p>
	 *
	 * @return {@code true} if this instance should be discarded and refreshed.
	 */
	boolean shouldReset();

	/**
	 * Check this instance against a frustum.<p>
	 * An implementor may choose to return a constant to skip the frustum check.
	 *
	 * @param frustum A frustum intersection tester for the current frame.
	 * @return {@code true} if this instance should be considered for updates.
	 */
	boolean checkFrustum(FrustumIntersection frustum);

	/**
	 * Calculate the distance squared between this instance and the given <em>world</em> position.
	 *
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 * @param z The z coordinate.
	 * @return The distance squared between this instance and the given position.
	 */
	double distanceSquared(double x, double y, double z);

	/**
	 * Free any acquired resources.
	 */
	void delete();

}
