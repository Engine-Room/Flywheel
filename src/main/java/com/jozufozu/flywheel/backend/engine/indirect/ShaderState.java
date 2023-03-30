package com.jozufozu.flywheel.backend.engine.indirect;

import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.vertex.VertexType;

public record ShaderState(Material material, VertexType vertex, StructType<?> instance) {
}
