package com.jozufozu.flywheel.backend.instancing;

/**
 * Something (a BlockEntity or Entity) that can be rendered using the instancing API.
 */
public interface InstanceRendered {

	/**
	 * @return true if there are parts of the renderer that cannot be implemented with Flywheel.
	 */
	default boolean shouldRenderNormally() {
		return false;
	}
}
