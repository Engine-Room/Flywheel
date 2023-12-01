package com.jozufozu.flywheel.api.material;

public enum Cutout {
	/**
	 * Do not discard any fragments based on alpha.
	 */
	OFF,
	/**
	 * Discard fragments with alpha close to or equal to zero.
	 */
	EPSILON,
	/**
	 * Discard fragments with alpha less than to 0.5.
	 */
	HALF,
}
