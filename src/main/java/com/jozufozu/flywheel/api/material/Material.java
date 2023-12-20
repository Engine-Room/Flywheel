package com.jozufozu.flywheel.api.material;

import net.minecraft.resources.ResourceLocation;

public interface Material {
	MaterialShaders shaders();

	FogShader fog();

	CutoutShader cutout();

	ResourceLocation texture();

	/**
	 * Should this material have linear filtering applied to the diffuse sampler?
	 *
	 * @return {@code true} if this material should be rendered with blur.
	 */
	boolean blur();

	boolean mipmap();

	/**
	 * Should this material be rendered with backface culling?
	 *
	 * @return {@code true} if this material should be rendered with backface culling.
	 */
	boolean backfaceCulling();

	boolean polygonOffset();

	DepthTest depthTest();

	Transparency transparency();

	WriteMask writeMask();

	boolean useOverlay();

	/**
	 * Should this material be rendered with block/sky lighting?
	 *
	 * @return {@code true} if this material should be rendered with block/sky lighting.
	 */
	boolean useLight();

	/**
	 * Should this material be rendered with diffuse lighting?
	 *
	 * @return {@code true} if this material should be rendered with diffuse lighting.
	 */
	boolean diffuse();
}
