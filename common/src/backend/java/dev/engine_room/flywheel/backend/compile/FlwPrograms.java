package dev.engine_room.flywheel.backend.compile;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.engine_room.flywheel.api.Flywheel;
import dev.engine_room.flywheel.backend.MaterialShaderIndices;
import dev.engine_room.flywheel.backend.compile.component.UberShaderComponent;
import dev.engine_room.flywheel.backend.compile.core.CompilerStats;
import dev.engine_room.flywheel.backend.compile.core.SourceLoader;
import dev.engine_room.flywheel.backend.glsl.ShaderSources;
import dev.engine_room.flywheel.backend.glsl.SourceComponent;
import dev.engine_room.flywheel.backend.glsl.generate.FnSignature;
import dev.engine_room.flywheel.backend.glsl.generate.GlslExpr;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public final class FlwPrograms {
	public static final Logger LOGGER = LoggerFactory.getLogger(Flywheel.ID + "/backend/shaders");

	private static final ResourceLocation COMPONENTS_HEADER_FRAG = Flywheel.rl("internal/components_header.frag");

	private FlwPrograms() {
	}

	static void reload(ResourceManager resourceManager) {
		// Reset the programs in case the ubershader load fails.
		InstancingPrograms.setInstance(null);
		IndirectPrograms.setInstance(null);

		var sources = new ShaderSources(resourceManager);
		var stats = new CompilerStats("ubershaders");
		var loader = new SourceLoader(sources, stats);

		var fragmentComponentsHeader = loader.find(COMPONENTS_HEADER_FRAG);

		var fogComponent = createFogComponent(loader);

		// TODO: separate compilation for cutout OFF, but keep the rest uber'd?
		if (stats.errored() || fragmentComponentsHeader == null || fogComponent == null) {
			// Probably means the shader sources are missing.
			stats.emitErrorLog();
			return;
		}

		List<SourceComponent> vertexComponents = List.of();
		List<SourceComponent> fragmentComponents = List.of(fragmentComponentsHeader, fogComponent);

		InstancingPrograms.reload(sources, vertexComponents, fragmentComponents);
		IndirectPrograms.reload(sources, vertexComponents, fragmentComponents);
	}

	@Nullable
	private static UberShaderComponent createFogComponent(SourceLoader loader) {
		return UberShaderComponent.builder(Flywheel.rl("fog"))
				.materialSources(MaterialShaderIndices.fogSources()
						.all())
				.adapt(FnSignature.create()
						.returnType("vec4")
						.name("flw_fogFilter")
						.arg("vec4", "color")
						.build(), GlslExpr.variable("color"))
				.switchOn(GlslExpr.variable("_flw_uberFogIndex"))
				.build(loader);
	}

	@Nullable
	private static UberShaderComponent createCutoutComponent(SourceLoader loader) {
		return UberShaderComponent.builder(Flywheel.rl("cutout"))
				.materialSources(MaterialShaderIndices.cutoutSources()
						.all())
				.adapt(FnSignature.create()
						.returnType("bool")
						.name("flw_discardPredicate")
						.arg("vec4", "color")
						.build(), GlslExpr.boolLiteral(false))
				.switchOn(GlslExpr.variable("_flw_uberCutoutIndex"))
				.build(loader);
	}
}
