package com.jozufozu.flywheel.backend.compile;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.backend.compile.component.IndirectComponent;
import com.jozufozu.flywheel.backend.compile.component.InstancedArraysComponent;
import com.jozufozu.flywheel.glsl.GLSLVersion;

public final class Pipelines {
	public static final Pipeline INSTANCED_ARRAYS = Pipeline.builder()
			.glslVersion(GLSLVersion.V330)
			.vertex(Flywheel.rl("internal/instancing/draw.vert"))
			.fragment(Flywheel.rl("internal/instancing/draw.frag"))
			.vertexAPI(Flywheel.rl("internal/instancing/api/vertex.glsl"))
			.fragmentAPI(Flywheel.rl("internal/instancing/api/fragment.glsl"))
			.assembler(InstancedArraysComponent::new)
			.build();
	public static final Pipeline INDIRECT = Pipeline.builder()
			.glslVersion(GLSLVersion.V460)
			.vertex(Flywheel.rl("internal/indirect/draw.vert"))
			.fragment(Flywheel.rl("internal/indirect/draw.frag"))
			.vertexAPI(Flywheel.rl("internal/indirect/api/vertex.glsl"))
			.fragmentAPI(Flywheel.rl("internal/indirect/api/fragment.glsl"))
			.assembler(IndirectComponent::create)
			.build();
}
