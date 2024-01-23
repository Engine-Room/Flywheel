package com.jozufozu.flywheel.api.event;

public enum RenderStage {
	AFTER_ENTITIES,
	AFTER_BLOCK_ENTITIES,
	AFTER_TRANSLUCENT_TERRAIN,
	AFTER_PARTICLES,
	AFTER_WEATHER;

	/**
	 * Is this stage the last one to be rendered in the frame?
	 *
	 * @return {@code true} if no other RenderStages will be dispatched this frame.
	 */
	public boolean isLast() {
		return this == values()[values().length - 1];
	}

	/**
	 * Is this stage the first one to be rendered in the frame?
	 *
	 * @return {@code true} if this is the first RenderStage to be dispatched this frame.
	 */
	public boolean isFirst() {
		return this == values()[0];
	}
}
