package com.jozufozu.flywheel.api.material;

public enum WriteMask {
	/**
	 * Write to both the color and depth buffers.
	 */
	BOTH,
	/**
	 * Write to the color buffer only.
	 */
	COLOR,
	/**
	 * Write to the depth buffer only.
	 */
	DEPTH,
	;

	public boolean depth() {
		return this == BOTH || this == DEPTH;
	}

	public boolean color() {
		return this == BOTH || this == COLOR;
	}
}
