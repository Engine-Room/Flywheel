package com.jozufozu.flywheel.backend.compile;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.internal.InternalFlywheelApi;
import com.jozufozu.flywheel.api.registry.Registry;
import com.jozufozu.flywheel.api.visualization.VisualEmbedding;

public class ContextShaders {
	public static final Registry<ContextShader> REGISTRY = InternalFlywheelApi.INSTANCE.createRegistry();
	public static final ContextShader DEFAULT = REGISTRY.registerAndGet(new ContextShader(Flywheel.rl("internal/context/default.vert"), Flywheel.rl("internal/context/default.frag")));
	public static final ContextShader CRUMBLING = REGISTRY.registerAndGet(new ContextShader(Flywheel.rl("internal/context/crumbling.vert"), Flywheel.rl("internal/context/crumbling.frag")));
	public static final ContextShader EMBEDDED = REGISTRY.registerAndGet(new ContextShader(Flywheel.rl("internal/context/embedded.vert"), Flywheel.rl("internal/context/embedded.frag")));

	public static ContextShader forEmbedding(@Nullable VisualEmbedding level) {
		if (level == null) {
			return DEFAULT;
		} else {
			return EMBEDDED;
		}
	}
}
