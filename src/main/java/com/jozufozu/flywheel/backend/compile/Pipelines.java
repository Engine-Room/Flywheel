package com.jozufozu.flywheel.backend.compile;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.backend.Samplers;
import com.jozufozu.flywheel.backend.compile.component.IndirectComponent;
import com.jozufozu.flywheel.backend.compile.component.SamplerBufferComponent;
import com.jozufozu.flywheel.backend.glsl.GlslVersion;

public final class Pipelines {
	public static final Pipeline INSTANCING = Pipeline.builder()
			.compilerMarker("instancing")
			.glslVersion(GlslVersion.V330)
			.vertexMain(Flywheel.rl("internal/instancing/main.vert"))
			.fragmentMain(Flywheel.rl("internal/instancing/main.frag"))
			.vertexApiImpl(Flywheel.rl("internal/instancing/api_impl.vert"))
			.fragmentApiImpl(Flywheel.rl("internal/instancing/api_impl.frag"))
			.assembler(SamplerBufferComponent::create)
			.onLink(program -> program.setSamplerBinding("_flw_instances", Samplers.INSTANCE_BUFFER))
			.build();
	public static final Pipeline INDIRECT = Pipeline.builder()
			.compilerMarker("indirect")
			.glslVersion(GlslVersion.V460)
			.vertexMain(Flywheel.rl("internal/indirect/main.vert"))
			.fragmentMain(Flywheel.rl("internal/indirect/main.frag"))
			.vertexApiImpl(Flywheel.rl("internal/indirect/api_impl.vert"))
			.fragmentApiImpl(Flywheel.rl("internal/indirect/api_impl.frag"))
			.assembler(IndirectComponent::create)
			.onLink($ -> {
			})
			.build();
}
