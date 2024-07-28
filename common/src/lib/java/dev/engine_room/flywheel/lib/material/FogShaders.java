package dev.engine_room.flywheel.lib.material;

import org.jetbrains.annotations.ApiStatus;

import dev.engine_room.flywheel.api.Flywheel;
import dev.engine_room.flywheel.api.material.FogShader;

public final class FogShaders {
	public static final FogShader NONE = FogShader.REGISTRY.registerAndGet(new SimpleFogShader(Flywheel.rl("fog/none.glsl")));
	public static final FogShader LINEAR = FogShader.REGISTRY.registerAndGet(new SimpleFogShader(Flywheel.rl("fog/linear.glsl")));
	public static final FogShader LINEAR_FADE = FogShader.REGISTRY.registerAndGet(new SimpleFogShader(Flywheel.rl("fog/linear_fade.glsl")));

	private FogShaders() {
	}

	@ApiStatus.Internal
	public static void init() {
	}
}
