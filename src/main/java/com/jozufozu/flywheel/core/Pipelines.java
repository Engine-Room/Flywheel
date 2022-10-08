package com.jozufozu.flywheel.core;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.backend.gl.GLSLVersion;
import com.jozufozu.flywheel.backend.instancing.indirect.IndirectComponent;
import com.jozufozu.flywheel.backend.instancing.instancing.InstancedArraysComponent;
import com.jozufozu.flywheel.core.pipeline.SimplePipeline;
import com.jozufozu.flywheel.core.source.FileResolution;

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
		public static final FileResolution DRAW_FRAGMENT = pipeline("pipeline/draw.frag");
		public static final FileResolution INSTANCED_ARRAYS_DRAW = pipeline("pipeline/instanced_arrays_draw.vert");
		public static final FileResolution INDIRECT_DRAW = pipeline("pipeline/indirect_draw.vert");
		public static final FileResolution INDIRECT_CULL = pipeline("pipeline/indirect_cull.glsl");
		public static final FileResolution UTIL_TYPES = FileResolution.get(Flywheel.rl("util/types.glsl"));

		private static FileResolution pipeline(String name) {
			return FileResolution.get(Flywheel.rl(name))
					.validateWith(Components.Checks.PIPELINE);
		}
	}
}
