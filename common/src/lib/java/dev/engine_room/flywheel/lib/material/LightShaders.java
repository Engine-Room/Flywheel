package dev.engine_room.flywheel.lib.material;

import org.jetbrains.annotations.ApiStatus;

import dev.engine_room.flywheel.api.Flywheel;
import dev.engine_room.flywheel.api.material.LightShader;

public class LightShaders {
	public static final LightShader SMOOTH_WHEN_EMBEDDED = LightShader.REGISTRY.registerAndGet(new SimpleLightShader(Flywheel.rl("light/smooth_when_embedded.glsl")));
	public static final LightShader SMOOTH = LightShader.REGISTRY.registerAndGet(new SimpleLightShader(Flywheel.rl("light/smooth.glsl")));
	public static final LightShader FLAT = LightShader.REGISTRY.registerAndGet(new SimpleLightShader(Flywheel.rl("light/flat.glsl")));

	private LightShaders() {
	}

	@ApiStatus.Internal
	public static void init() {
	}
}
