package dev.engine_room.flywheel.lib.material;

import dev.engine_room.flywheel.api.material.FogShader;
import dev.engine_room.flywheel.lib.util.IdentifierUtil;

public final class FogShaders {
	public static final FogShader NONE = new SimpleFogShader(IdentifierUtil.id("fog/none.glsl"));
	public static final FogShader LINEAR = new SimpleFogShader(IdentifierUtil.id("fog/linear.glsl"));
	public static final FogShader LINEAR_FADE = new SimpleFogShader(IdentifierUtil.id("fog/linear_fade.glsl"));

	private FogShaders() {
	}
}
