package com.jozufozu.flywheel.backend.engine.instancing;

import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.vertex.VertexType;

public record ShaderState(Material material, VertexType vertexType, InstanceType<?> instanceType) {
}
