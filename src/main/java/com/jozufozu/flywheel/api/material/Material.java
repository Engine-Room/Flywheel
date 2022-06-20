package com.jozufozu.flywheel.api.material;

import com.jozufozu.flywheel.core.source.FileResolution;

import net.minecraft.client.renderer.RenderType;

public record Material(RenderType renderType, FileResolution vertexShader, FileResolution fragmentShader) {

}
