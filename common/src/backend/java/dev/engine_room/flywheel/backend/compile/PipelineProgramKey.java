package dev.engine_room.flywheel.backend.compile;

import dev.engine_room.flywheel.api.instance.InstanceType;

/**
 * Represents the entire context of a program's usage.
 *
 * @param instanceType  The instance shader to use.
 * @param contextShader The context shader to use.
 */
public record PipelineProgramKey(InstanceType<?> instanceType, ContextShader contextShader) {
}
