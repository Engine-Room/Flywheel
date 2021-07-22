package com.jozufozu.flywheel.backend.state;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.backend.gl.GlTextureUnit;
import com.jozufozu.flywheel.backend.material.MaterialManager;
import com.jozufozu.flywheel.backend.material.MaterialGroup;
import com.jozufozu.flywheel.core.shader.WorldProgram;

import net.minecraft.util.ResourceLocation;

public interface IRenderState {

	void bind();

	void unbind();

	@Nullable
	default ResourceLocation getTexture(GlTextureUnit textureUnit) {
		return null;
	}
}
