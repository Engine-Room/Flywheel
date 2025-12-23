package dev.engine_room.flywheel.api.material;

import net.minecraft.resources.Identifier;

public interface Material {
	MaterialShaders shaders();

	FogShader fog();

	CutoutShader cutout();

	LightShader light();

	Identifier texture();

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
	 * How should this material receive cardinal lighting?
	 *
	 * @return The cardinal lighting mode.
	 */
	CardinalLightingMode cardinalLightingMode();
}
