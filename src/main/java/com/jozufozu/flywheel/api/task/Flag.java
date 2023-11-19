package com.jozufozu.flywheel.api.task;

/**
 * Marker interface for flags that can be raised and lowered in a {@link TaskExecutor}.
 * <br>
 * <strong>Warning:</strong> flags will only be considered equal by reference.
 * This is to allow multiple instances of the same high level structures to exist at once.
 * <br>
 * Keep this in mind when using records as flags.
 */
public interface Flag {
}
