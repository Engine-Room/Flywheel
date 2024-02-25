package com.jozufozu.flywheel.backend.compile;

import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.backend.context.ContextShader;

/**
 * Represents the entire context of a program's usage.
 *
 * @param instanceType  The instance shader to use.
 * @param contextShader The context shader to use.
 */
public record PipelineProgramKey(InstanceType<?> instanceType, ContextShader contextShader) {
}
