package com.jozufozu.flywheel.lib.context;

import org.jetbrains.annotations.ApiStatus;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.context.ContextShader;

public class ContextShaders {
	public static final SimpleContextShader DEFAULT = ContextShader.REGISTRY.registerAndGet(new SimpleContextShader(Flywheel.rl("context/default.vert"), Flywheel.rl("context/default.frag")));
	public static final SimpleContextShader CRUMBLING = ContextShader.REGISTRY.registerAndGet(new SimpleContextShader(Flywheel.rl("context/crumbling.vert"), Flywheel.rl("context/crumbling.frag")));

	private ContextShaders() {
	}

	@ApiStatus.Internal
	public static void init() {
	}
}
