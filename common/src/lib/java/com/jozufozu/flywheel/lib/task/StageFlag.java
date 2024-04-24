package com.jozufozu.flywheel.lib.task;

import com.jozufozu.flywheel.api.event.RenderStage;

/**
 * A flag that is associated with a render stage.
 * <br>
 * Useful for synchronizing tasks for a specific render stage.
 */
public final class StageFlag extends Flag {
	private final RenderStage stage;

	/**
	 * @param stage The render stage this flag is associated with.
	 */
	public StageFlag(RenderStage stage) {
		this.stage = stage;
	}

	@Override
	public String toString() {
		return "StageFlag[" + "stage=" + stage + ']';
	}

}
