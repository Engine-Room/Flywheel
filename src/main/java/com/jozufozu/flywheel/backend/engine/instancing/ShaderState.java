package com.jozufozu.flywheel.backend.engine.instancing;

import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.material.Material;

public record ShaderState(Material material, InstanceType<?> instanceType) {
}
