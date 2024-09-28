package dev.engine_room.flywheel.lib.material;

import dev.engine_room.flywheel.api.Flywheel;
import dev.engine_room.flywheel.api.material.FogShader;

public final class FogShaders {
	public static final FogShader NONE = new SimpleFogShader(Flywheel.rl("fog/none.glsl"));
	public static final FogShader LINEAR = new SimpleFogShader(Flywheel.rl("fog/linear.glsl"));
	public static final FogShader LINEAR_FADE = new SimpleFogShader(Flywheel.rl("fog/linear_fade.glsl"));

	private FogShaders() {
	}
}
