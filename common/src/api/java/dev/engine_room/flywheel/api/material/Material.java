package dev.engine_room.flywheel.api.material;

import org.jetbrains.annotations.Nullable;

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

	/**
	 * Whether this material should receive ambient occlusion from nearby chunk geometry.
	 *
	 * @return {@code true} if this material should receive ambient occlusion.
	 */
	default boolean ambientOcclusion() {
		return true;
	}

	/**
	 * Check for field-wise equality between this Material and another.
	 *
	 * @param other The nullable material to check equality against.
	 * @return True if the materials represent the same configuration.
	 */
	default boolean equals(@Nullable Material other) {
		if (this == other) {
			return true;
		}

		if (other == null) {
			return false;
		}

		// @formatter:off
		return this.blur() == other.blur()
				&& this.mipmap() == other.mipmap()
				&& this.backfaceCulling() == other.backfaceCulling()
				&& this.polygonOffset() == other.polygonOffset()
				&& this.depthTest() == other.depthTest()
				&& this.transparency() == other.transparency()
				&& this.writeMask() == other.writeMask()
				&& this.useOverlay() == other.useOverlay()
				&& this.useLight() == other.useLight()
				&& this.cardinalLightingMode() == other.cardinalLightingMode()
				&& this.ambientOcclusion() == other.ambientOcclusion()
				&& this.shaders().fragmentSource().equals(other.shaders().fragmentSource())
				&& this.shaders().vertexSource().equals(other.shaders().vertexSource())
				&& this.fog().source().equals(other.fog().source())
				&& this.cutout().source().equals(other.cutout().source())
				&& this.light().source().equals(other.light().source())
				&& this.texture().equals(other.texture());
		// @formatter:on
	}
}
