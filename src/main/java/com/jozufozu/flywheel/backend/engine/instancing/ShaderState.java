package com.jozufozu.flywheel.backend.engine.instancing;

import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.backend.context.Context;

public record ShaderState(Material material, InstanceType<?> instanceType, Context context) {
}
