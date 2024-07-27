package dev.engine_room.flywheel.backend.compile;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

import dev.engine_room.flywheel.api.Flywheel;
import dev.engine_room.flywheel.api.instance.InstanceType;
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

	private static final ResourceLocation COMPONENTS_HEADER_VERT = Flywheel.rl("internal/components_header.vert");
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

		var vertexComponentsHeader = loader.find(COMPONENTS_HEADER_VERT);
		var fragmentComponentsHeader = loader.find(COMPONENTS_HEADER_FRAG);

		var vertexMaterialComponent = createVertexMaterialComponent(loader);
		var fragmentMaterialComponent = createFragmentMaterialComponent(loader);
		var fogComponent = createFogComponent(loader);
		var cutoutComponent = createCutoutComponent(loader);
		var lightComponent = createLightComponent(loader);

		if (stats.errored() || vertexComponentsHeader == null || fragmentComponentsHeader == null || vertexMaterialComponent == null || fragmentMaterialComponent == null || fogComponent == null || cutoutComponent == null || lightComponent == null) {
			// Probably means the shader sources are missing.
			stats.emitErrorLog();
			return;
		}

		List<SourceComponent> vertexComponents = List.of(vertexComponentsHeader, vertexMaterialComponent);
		List<SourceComponent> fragmentComponents = List.of(fragmentComponentsHeader, fragmentMaterialComponent, fogComponent, cutoutComponent, lightComponent);

		var pipelineKeys = createPipelineKeys();
		InstancingPrograms.reload(sources, pipelineKeys, vertexComponents, fragmentComponents);
		IndirectPrograms.reload(sources, pipelineKeys, vertexComponents, fragmentComponents);
	}

	private static ImmutableList<PipelineProgramKey> createPipelineKeys() {
		ImmutableList.Builder<PipelineProgramKey> builder = ImmutableList.builder();
		for (ContextShader contextShader : ContextShader.values()) {
			for (InstanceType<?> instanceType : InstanceType.REGISTRY) {
				builder.add(new PipelineProgramKey(instanceType, contextShader));
			}
		}
		return builder.build();
	}

	@Nullable
	private static UberShaderComponent createVertexMaterialComponent(SourceLoader loader) {
		return UberShaderComponent.builder(Flywheel.rl("material_vertex"))
				.materialSources(MaterialShaderIndices.vertexSources()
						.all())
				.adapt(FnSignature.ofVoid("flw_materialVertex"))
				.switchOn(GlslExpr.variable("_flw_uberMaterialVertexIndex"))
				.build(loader);
	}

	@Nullable
	private static UberShaderComponent createFragmentMaterialComponent(SourceLoader loader) {
		return UberShaderComponent.builder(Flywheel.rl("material_fragment"))
				.materialSources(MaterialShaderIndices.fragmentSources()
						.all())
				.adapt(FnSignature.ofVoid("flw_materialFragment"))
				.switchOn(GlslExpr.variable("_flw_uberMaterialFragmentIndex"))
				.build(loader);
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

	@Nullable
	private static UberShaderComponent createLightComponent(SourceLoader loader) {
		return UberShaderComponent.builder(Flywheel.rl("light"))
				.materialSources(MaterialShaderIndices.lightSources()
						.all())
				.adapt(FnSignature.create()
						.returnType("void")
						.name("flw_shaderLight")
						.build())
				.switchOn(GlslExpr.variable("_flw_uberLightIndex"))
				.build(loader);
	}
}
