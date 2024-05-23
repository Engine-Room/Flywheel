package com.jozufozu.flywheel.lib.material;

import org.jetbrains.annotations.ApiStatus;

import com.jozufozu.flywheel.api.Flywheel;
import com.jozufozu.flywheel.api.material.FogShader;

public class FogShaders {
	public static final FogShader NONE = FogShader.REGISTRY.registerAndGet(new SimpleFogShader(Flywheel.rl("fog/none.glsl")));
	public static final FogShader LINEAR = FogShader.REGISTRY.registerAndGet(new SimpleFogShader(Flywheel.rl("fog/linear.glsl")));
	public static final FogShader LINEAR_FADE = FogShader.REGISTRY.registerAndGet(new SimpleFogShader(Flywheel.rl("fog/linear_fade.glsl")));

	private FogShaders() {
	}

	@ApiStatus.Internal
	public static void init() {
	}
}
