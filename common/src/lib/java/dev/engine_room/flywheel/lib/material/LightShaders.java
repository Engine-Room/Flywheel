package dev.engine_room.flywheel.lib.material;

import dev.engine_room.flywheel.api.Flywheel;
import dev.engine_room.flywheel.api.material.LightShader;

public final class LightShaders {
	public static final LightShader SMOOTH_WHEN_EMBEDDED = new SimpleLightShader(Flywheel.rl("light/smooth_when_embedded.glsl"));
	public static final LightShader SMOOTH = new SimpleLightShader(Flywheel.rl("light/smooth.glsl"));
	public static final LightShader FLAT = new SimpleLightShader(Flywheel.rl("light/flat.glsl"));

	private LightShaders() {
	}
}
