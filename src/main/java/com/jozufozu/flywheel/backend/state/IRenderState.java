package com.jozufozu.flywheel.backend.state;

import com.jozufozu.flywheel.backend.gl.GlTextureUnit;

import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;

public interface IRenderState {

	void bind();

	void unbind();

	@Nullable
	default ResourceLocation getTexture(GlTextureUnit textureUnit) {
		return null;
	}
}
