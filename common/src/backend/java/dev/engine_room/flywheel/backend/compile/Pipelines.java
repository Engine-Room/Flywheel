package dev.engine_room.flywheel.backend.compile;

import dev.engine_room.flywheel.backend.Samplers;
import dev.engine_room.flywheel.backend.compile.component.BufferTextureInstanceComponent;
import dev.engine_room.flywheel.backend.compile.component.SsboInstanceComponent;
import dev.engine_room.flywheel.lib.util.IdentifierUtil;

public final class Pipelines {
	public static final Pipeline INSTANCING = Pipeline.builder()
			.compilerMarker("instancing")
			.vertexMain(IdentifierUtil.id("internal/instancing/main.vert"))
			.fragmentMain(IdentifierUtil.id("internal/instancing/main.frag"))
			.assembler(BufferTextureInstanceComponent::new)
			.onLink(program -> {
				program.setSamplerBinding("_flw_instances", Samplers.INSTANCE_BUFFER);
				program.setSamplerBinding("_flw_lightLut", Samplers.LIGHT_LUT);
				program.setSamplerBinding("_flw_lightSections", Samplers.LIGHT_SECTIONS);
			})
			.build();

	public static final Pipeline INDIRECT = Pipeline.builder()
			.compilerMarker("indirect")
			.vertexMain(IdentifierUtil.id("internal/indirect/main.vert"))
			.fragmentMain(IdentifierUtil.id("internal/indirect/main.frag"))
			.assembler(SsboInstanceComponent::new)
			.onLink($ -> {
			})
			.build();

	private Pipelines() {
	}
}
