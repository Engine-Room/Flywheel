package com.jozufozu.flywheel.backend.engine;

import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.backend.engine.embed.Environment;

public record GroupKey<I extends Instance>(InstanceType<I> instanceType, Environment environment) {
}
