package com.jozufozu.flywheel.lib.material;

import org.jetbrains.annotations.ApiStatus;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.material.CutoutShader;

public class CutoutShaders {
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

	private CutoutShaders() {
	}

	@ApiStatus.Internal
	public static void init() {
	}
}
