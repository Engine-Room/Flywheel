package com.jozufozu.flywheel.lib.material;

import org.jetbrains.annotations.ApiStatus;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.material.CutoutShader;
import com.jozufozu.flywheel.api.material.FogShader;
import com.jozufozu.flywheel.api.material.MaterialShaders;

import net.minecraft.resources.ResourceLocation;

public final class StandardMaterialShaders {
	/**
	 * Do not discard any fragments based on alpha.
	 */
	public static final CutoutShader OFF = CutoutShader.REGISTRY.registerAndGet(new SimpleCutoutShader(Flywheel.rl("cutout/off.glsl")));
	/**
	 * Discard fragments with alpha close to or equal to zero.
	 */
	public static final CutoutShader EPSILON = CutoutShader.REGISTRY.registerAndGet(new SimpleCutoutShader(Flywheel.rl("cutout/epsilon.glsl")));
	/**
	 * Discard fragments with alpha less than to 0.5.
	 */
	public static final CutoutShader HALF = CutoutShader.REGISTRY.registerAndGet(new SimpleCutoutShader(Flywheel.rl("cutout/half.glsl")));

	public static final FogShader NONE = FogShader.REGISTRY.registerAndGet(new SimpleFogShader(Flywheel.rl("fog/none.glsl")));
	public static final FogShader LINEAR = FogShader.REGISTRY.registerAndGet(new SimpleFogShader(Flywheel.rl("fog/linear.glsl")));
	public static final FogShader LINEAR_FADE = FogShader.REGISTRY.registerAndGet(new SimpleFogShader(Flywheel.rl("fog/linear_fade.glsl")));

	public static final MaterialShaders DEFAULT = MaterialShaders.REGISTRY.registerAndGet(new SimpleMaterialShaders(Files.DEFAULT_VERTEX, Files.DEFAULT_FRAGMENT));

	private StandardMaterialShaders() {
	}

	@ApiStatus.Internal
	public static void init() {
	}

	public static final class Files {
		public static final ResourceLocation DEFAULT_VERTEX = Names.DEFAULT.withSuffix(".vert");
		public static final ResourceLocation DEFAULT_FRAGMENT = Names.DEFAULT.withSuffix(".frag");
	}

	public static final class Names {
		public static final ResourceLocation DEFAULT = Flywheel.rl("material/default");
	}
}
