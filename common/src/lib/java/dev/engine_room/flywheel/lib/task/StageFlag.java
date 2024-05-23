package dev.engine_room.flywheel.lib.task;

import dev.engine_room.flywheel.api.event.RenderStage;

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
