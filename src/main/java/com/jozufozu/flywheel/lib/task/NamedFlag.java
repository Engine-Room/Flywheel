package com.jozufozu.flywheel.lib.task;

import com.jozufozu.flywheel.api.task.Flag;

/**
 * A flag with an arbitrary name.
 *
 * @param name The name of the flag, mainly for debugging purposes.
 */
public record NamedFlag(String name) implements Flag {
}
