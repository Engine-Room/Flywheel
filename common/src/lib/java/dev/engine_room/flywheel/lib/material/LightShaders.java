package dev.engine_room.flywheel.lib.material;

import dev.engine_room.flywheel.api.material.LightShader;
import dev.engine_room.flywheel.lib.util.ResourceUtil;

public final class LightShaders {
	public static final LightShader SMOOTH_WHEN_EMBEDDED = new SimpleLightShader(ResourceUtil.rl("light/smooth_when_embedded.glsl"));
	public static final LightShader SMOOTH = new SimpleLightShader(ResourceUtil.rl("light/smooth.glsl"));
	public static final LightShader FLAT = new SimpleLightShader(ResourceUtil.rl("light/flat.glsl"));

	private LightShaders() {
	}
}
