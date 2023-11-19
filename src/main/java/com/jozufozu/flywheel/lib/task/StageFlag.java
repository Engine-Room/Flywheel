package com.jozufozu.flywheel.lib.task;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.task.Flag;

/**
 * A flag that is associated with a render stage.
 * <br>
 * Useful for synchronizing tasks for a specific render stage.
 */
public record StageFlag(RenderStage stage) implements Flag {
}
