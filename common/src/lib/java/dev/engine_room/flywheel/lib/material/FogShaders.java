package dev.engine_room.flywheel.lib.material;

import dev.engine_room.flywheel.api.material.FogShader;
import dev.engine_room.flywheel.lib.util.ResourceUtil;

public final class FogShaders {
	public static final FogShader NONE = new SimpleFogShader(ResourceUtil.rl("fog/none.glsl"));
	public static final FogShader LINEAR = new SimpleFogShader(ResourceUtil.rl("fog/linear.glsl"));
	public static final FogShader LINEAR_FADE = new SimpleFogShader(ResourceUtil.rl("fog/linear_fade.glsl"));

	private FogShaders() {
	}
}
