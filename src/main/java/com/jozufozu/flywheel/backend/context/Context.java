package com.jozufozu.flywheel.backend.context;

import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.backend.engine.textures.TextureSource;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;

public interface Context {
	ContextShader contextShader();

	/**
	 * Prepare the shader for rendering with the given material and textures.
	 *
	 * @param material The material about to be rendered.
	 * @param shader   The shader to prepare.
	 * @param textureSource Source of the textures to use.
	 */
	void prepare(Material material, GlProgram shader, TextureSource textureSource);
}
