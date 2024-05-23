package dev.engine_room.flywheel.backend.engine;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.backend.engine.embed.Environment;

public record GroupKey<I extends Instance>(InstanceType<I> instanceType, Environment environment) {
}
