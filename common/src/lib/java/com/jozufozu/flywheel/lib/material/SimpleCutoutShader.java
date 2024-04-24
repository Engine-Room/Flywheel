package com.jozufozu.flywheel.lib.material;

import com.jozufozu.flywheel.api.material.CutoutShader;

import net.minecraft.resources.ResourceLocation;

public record SimpleCutoutShader(@Override ResourceLocation source) implements CutoutShader {
}
