package com.jozufozu.flywheel.api.material;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public interface Material {
	RenderType getFallbackRenderType();

	MaterialVertexTransformer getVertexTransformer();

	MaterialShaders shaders();

	ResourceLocation baseTexture();

	/**
	 * Should this material be rendered with diffuse lighting?
	 *
	 * @return {@code true} if this material should be rendered with diffuse lighting.
	 */
	boolean diffuse();

	/**
	 * Should this material be rendered with block/sky lighting?
	 *
	 * @return {@code true} if this material should be rendered with block/sky lighting.
	 */
	boolean lighting();

	/**
	 * Should this material have linear filtering applied to the diffuse sampler?
	 *
	 * @return {@code true} if this material should be rendered with blur.
	 */
	boolean blur();

	/**
	 * Should this material be rendered with backface culling?
	 *
	 * @return {@code true} if this material should be rendered with backface culling.
	 */
	boolean backfaceCull();

	boolean polygonOffset();

	boolean mip();

	Fog fog();

	Transparency transparency();

	Cutout cutout();

	WriteMask writeMask();
}
