package dev.engine_room.flywheel.backend.compile;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.engine_room.flywheel.api.Flywheel;
import dev.engine_room.flywheel.backend.compile.core.CompilerStats;
import dev.engine_room.flywheel.backend.glsl.ShaderSources;
import dev.engine_room.flywheel.backend.glsl.SourceComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public final class FlwPrograms {
	public static final Logger LOGGER = LoggerFactory.getLogger(Flywheel.ID + "/backend/shaders");

	private static final ResourceLocation COMPONENTS_HEADER_FRAG = Flywheel.rl("internal/components_header.frag");

	public static ShaderSources SOURCES;

	private FlwPrograms() {
	}

	static void reload(ResourceManager resourceManager) {
		// Reset the programs in case the ubershader load fails.
		InstancingPrograms.setInstance(null);
		IndirectPrograms.setInstance(null);

		var sources = new ShaderSources(resourceManager);
		SOURCES = sources;
		var stats = new CompilerStats("ubershaders");

		var fragmentComponentsHeader = sources.get(COMPONENTS_HEADER_FRAG);

		// TODO: separate compilation for cutout OFF, but keep the rest uber'd?
		if (stats.errored() || fragmentComponentsHeader == null) {
			// Probably means the shader sources are missing.
			stats.emitErrorLog();
			return;
		}

		List<SourceComponent> vertexComponents = List.of();
		List<SourceComponent> fragmentComponents = List.of(fragmentComponentsHeader);

		InstancingPrograms.reload(sources, vertexComponents, fragmentComponents);
		IndirectPrograms.reload(sources, vertexComponents, fragmentComponents);
	}
}
