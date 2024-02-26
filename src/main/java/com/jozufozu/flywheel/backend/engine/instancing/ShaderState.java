package com.jozufozu.flywheel.backend.engine.instancing;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.visualization.VisualEmbedding;

public record ShaderState(Material material, InstanceType<?> instanceType, @Nullable VisualEmbedding embedding) {
}
