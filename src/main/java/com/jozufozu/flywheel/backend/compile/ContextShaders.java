package com.jozufozu.flywheel.backend.compile;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.internal.InternalFlywheelApi;
import com.jozufozu.flywheel.api.registry.Registry;
import com.jozufozu.flywheel.api.visualization.VisualEmbedding;
import com.jozufozu.flywheel.backend.Samplers;

public class ContextShaders {
	public static final Registry<ContextShader> REGISTRY = InternalFlywheelApi.INSTANCE.createRegistry();
	public static final ContextShader DEFAULT = REGISTRY.registerAndGet(ContextShader.builder()
			.vertexShader(Flywheel.rl("internal/context/default.vert"))
			.fragmentShader(Flywheel.rl("internal/context/default.frag"))
			.onLink($ -> {
			})
			.build());
	public static final ContextShader CRUMBLING = REGISTRY.registerAndGet(ContextShader.builder()
			.vertexShader(Flywheel.rl("internal/context/crumbling.vert"))
			.fragmentShader(Flywheel.rl("internal/context/crumbling.frag"))
			.onLink(program -> program.setSamplerBinding("_flw_crumblingTex", Samplers.CRUMBLING))
			.build());
	public static final ContextShader EMBEDDED = REGISTRY.registerAndGet(ContextShader.builder()
			.vertexShader(Flywheel.rl("internal/context/embedded.vert"))
			.fragmentShader(Flywheel.rl("internal/context/embedded.frag"))
			.onLink($ -> {
			})
			.build());

	public static ContextShader forEmbedding(@Nullable VisualEmbedding level) {
		if (level == null) {
			return DEFAULT;
		} else {
			return EMBEDDED;
		}
	}
}
