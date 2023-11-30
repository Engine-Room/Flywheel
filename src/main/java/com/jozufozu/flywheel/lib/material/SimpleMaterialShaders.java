package com.jozufozu.flywheel.lib.material;

import com.jozufozu.flywheel.api.material.MaterialShaders;

import net.minecraft.resources.ResourceLocation;

public record SimpleMaterialShaders(ResourceLocation vertexShader, ResourceLocation fragmentShader) implements MaterialShaders {
}
