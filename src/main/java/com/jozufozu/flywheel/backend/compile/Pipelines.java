package com.jozufozu.flywheel.backend.compile;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.backend.compile.component.IndirectComponent;
import com.jozufozu.flywheel.backend.compile.component.InstancedArraysComponent;
import com.jozufozu.flywheel.glsl.GLSLVersion;

import net.minecraft.resources.ResourceLocation;

public final class Pipelines {
	public static final Pipeline INSTANCED_ARRAYS = Pipeline.builder()
			.glslVersion(GLSLVersion.V420)
			.vertex(Files.INSTANCED_ARRAYS_DRAW)
			.fragment(Files.DRAW_FRAGMENT)
			.assembler(InstancedArraysComponent::new)
			.build();
	public static final Pipeline INDIRECT = Pipeline.builder()
			.glslVersion(GLSLVersion.V460)
			.vertex(Files.INDIRECT_DRAW)
			.fragment(Files.DRAW_FRAGMENT)
			.assembler(IndirectComponent::create)
			.build();

	public static void init() {
	}

	public static final class Files {
		public static final ResourceLocation INSTANCED_ARRAYS_DRAW = Flywheel.rl("internal/instanced_arrays_draw.vert");
		public static final ResourceLocation INDIRECT_DRAW = Flywheel.rl("internal/indirect_draw.vert");
		public static final ResourceLocation DRAW_FRAGMENT = Flywheel.rl("internal/draw.frag");

		public static final ResourceLocation UTIL_TYPES = Flywheel.rl("util/types.glsl");
	}
}
