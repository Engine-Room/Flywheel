package com.jozufozu.flywheel.api.context;

import com.jozufozu.flywheel.api.BackendImplemented;

import net.minecraft.resources.ResourceLocation;

@BackendImplemented
public interface Textures {
	/**
	 * Get a built-in texture by its resource location.
	 *
	 * @param texture The texture's resource location.
	 * @return The texture.
	 */
	Texture byName(ResourceLocation texture);

	/**
	 * Get the overlay texture.
	 *
	 * @return The overlay texture.
	 */
	Texture overlay();

	/**
	 * Get the light texture.
	 *
	 * @return The light texture.
	 */
	Texture light();

	// TODO: Allow creating dynamic textures.
}
