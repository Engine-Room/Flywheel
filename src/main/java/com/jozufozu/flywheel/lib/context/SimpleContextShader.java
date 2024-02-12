package com.jozufozu.flywheel.lib.context;

import com.jozufozu.flywheel.api.context.ContextShader;

import net.minecraft.resources.ResourceLocation;

public record SimpleContextShader(ResourceLocation vertexShader,
								  ResourceLocation fragmentShader) implements ContextShader {
	@Override
	public ResourceLocation vertexShader() {
		return vertexShader;
	}

	@Override
	public ResourceLocation fragmentShader() {
		return fragmentShader;
	}
}
