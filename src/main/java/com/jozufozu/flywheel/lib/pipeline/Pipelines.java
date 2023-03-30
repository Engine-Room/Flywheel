package com.jozufozu.flywheel.lib.pipeline;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.backend.engine.indirect.IndirectComponent;
import com.jozufozu.flywheel.backend.engine.instancing.InstancedArraysComponent;
import com.jozufozu.flywheel.gl.GLSLVersion;

import net.minecraft.resources.ResourceLocation;

public class Pipelines {
	public static final SimplePipeline INSTANCED_ARRAYS = SimplePipeline.builder()
			.glslVersion(GLSLVersion.V420)
			.vertex(Files.INSTANCED_ARRAYS_DRAW)
			.fragment(Files.DRAW_FRAGMENT)
			.assemblerFactory(InstancedArraysComponent::new)
			.build();
	public static final SimplePipeline INDIRECT = SimplePipeline.builder()
			.glslVersion(GLSLVersion.V460)
			.vertex(Files.INDIRECT_DRAW)
			.fragment(Files.DRAW_FRAGMENT)
			.assemblerFactory(IndirectComponent::new)
			.build();

	public static void init() {
		// noop
	}

	public static class Files {
		public static final ResourceLocation DRAW_FRAGMENT = Flywheel.rl("pipeline/draw.frag");
		public static final ResourceLocation INSTANCED_ARRAYS_DRAW = Flywheel.rl("pipeline/instanced_arrays_draw.vert");
		public static final ResourceLocation INDIRECT_DRAW = Flywheel.rl("pipeline/indirect_draw.vert");
		public static final ResourceLocation INDIRECT_CULL = Flywheel.rl("pipeline/indirect_cull.glsl");
		public static final ResourceLocation UTIL_TYPES = Flywheel.rl("util/types.glsl");
	}
}
