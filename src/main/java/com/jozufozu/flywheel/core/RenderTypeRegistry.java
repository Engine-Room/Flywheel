package com.jozufozu.flywheel.core;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.RenderType;

public class RenderTypeRegistry {


	/**
	 * Gets the alpha discard threshold for the given render layer.
	 *
	 * @param layer The render layer to get the alpha discard threshold for.
	 * @return The alpha discard threshold.
	 */
	public static float getAlphaDiscard(@Nullable RenderType layer) {
		return layer == RenderType.cutoutMipped() ? 0.1f : 0f;
	}
}
