package com.jozufozu.flywheel.backend.context;

import org.jetbrains.annotations.ApiStatus;

import com.jozufozu.flywheel.Flywheel;

public class ContextShaders {
	public static final SimpleContextShader DEFAULT = ContextShader.REGISTRY.registerAndGet(new SimpleContextShader(Flywheel.rl("internal/context/default.vert"), Flywheel.rl("internal/context/default.frag")));
	public static final SimpleContextShader CRUMBLING = ContextShader.REGISTRY.registerAndGet(new SimpleContextShader(Flywheel.rl("internal/context/crumbling.vert"), Flywheel.rl("internal/context/crumbling.frag")));
	public static final SimpleContextShader EMBEDDED = ContextShader.REGISTRY.registerAndGet(new SimpleContextShader(Flywheel.rl("internal/context/embedded.vert"), Flywheel.rl("internal/context/embedded.frag")));

	private ContextShaders() {
	}

	@ApiStatus.Internal
	public static void init() {
	}
}
