package com.jozufozu.flywheel.lib.context;

import org.jetbrains.annotations.ApiStatus;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.context.Context;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;

public final class Contexts {
	public static final SimpleContext DEFAULT = Context.REGISTRY.registerAndGet(new SimpleContext(
			Flywheel.rl("context/default.vert"),
			Flywheel.rl("context/default.frag"),
			program -> {
				program.bind();
				program.setSamplerBinding("_flw_diffuseTex", 0);
				program.setSamplerBinding("_flw_overlayTex", 1);
				program.setSamplerBinding("_flw_lightTex", 2);
				GlProgram.unbind();
			}));

	public static final SimpleContext CRUMBLING = Context.REGISTRY.registerAndGet(new SimpleContext(
			Flywheel.rl("context/crumbling.vert"),
			Flywheel.rl("context/crumbling.frag"),
			program -> {
				program.bind();
				program.setSamplerBinding("_flw_crumblingTex", 0);
				program.setSamplerBinding("_flw_diffuseTex", 1);
				GlProgram.unbind();
			}));

	private Contexts() {
	}

	@ApiStatus.Internal
	public static void init() {
	}
}
