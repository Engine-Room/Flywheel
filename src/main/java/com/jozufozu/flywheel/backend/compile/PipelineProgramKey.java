package com.jozufozu.flywheel.backend.compile;

import com.jozufozu.flywheel.api.context.Context;
import com.jozufozu.flywheel.api.instance.InstanceType;

/**
 * Represents the entire context of a program's usage.
 *
 * @param instanceType  The instance shader to use.
 * @param contextShader The context shader to use.
 */
public record PipelineProgramKey(InstanceType<?> instanceType, Context contextShader) {
}
