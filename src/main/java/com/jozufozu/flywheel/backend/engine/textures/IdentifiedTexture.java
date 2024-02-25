package com.jozufozu.flywheel.backend.engine.textures;

import com.jozufozu.flywheel.backend.context.Texture;

/**
 * Internal base interface that {@link com.jozufozu.flywheel.backend.gl.shader.GlProgram GlProgram} expects.
 */
public interface IdentifiedTexture extends Texture {
	/**
	 * @return The GL texture id of this texture.
	 */
	int id();
}
