package com.jozufozu.flywheel.api.context;

import com.jozufozu.flywheel.api.material.Material;

public interface Context {
	ContextShader contextShader();

	/**
	 * Prepare the shader for rendering with the given material and textures.
	 *
	 * @param material The material about to be rendered.
	 * @param shader   The shader to prepare.
	 * @param textureSource Source of the textures to use.
	 */
	void prepare(Material material, Shader shader, TextureSource textureSource);
}
