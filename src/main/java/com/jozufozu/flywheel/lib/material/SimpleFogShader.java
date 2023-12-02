package com.jozufozu.flywheel.lib.material;

import com.jozufozu.flywheel.api.material.FogShader;

import net.minecraft.resources.ResourceLocation;

public record SimpleFogShader(@Override ResourceLocation source) implements FogShader {
}
