package dev.engine_room.flywheel.api.material;

public enum WriteMask {
	/**
	 * Write to both the color and depth buffers.
	 */
	COLOR_DEPTH,
	/**
	 * Write to the color buffer only.
	 */
	COLOR,
	/**
	 * Write to the depth buffer only.
	 */
	DEPTH,
	;

	public boolean color() {
		return this == COLOR_DEPTH || this == COLOR;
	}

	public boolean depth() {
		return this == COLOR_DEPTH || this == DEPTH;
	}
}
