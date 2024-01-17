package com.jozufozu.flywheel.backend.compile;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.context.Context;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.uniform.ShaderUniforms;
import com.jozufozu.flywheel.backend.ShaderIndices;
import com.jozufozu.flywheel.backend.compile.component.UberShaderComponent;
import com.jozufozu.flywheel.backend.compile.component.UniformComponent;
import com.jozufozu.flywheel.backend.compile.core.CompilerStats;
import com.jozufozu.flywheel.backend.compile.core.SourceLoader;
import com.jozufozu.flywheel.backend.glsl.ShaderSources;
import com.jozufozu.flywheel.backend.glsl.SourceComponent;
import com.jozufozu.flywheel.backend.glsl.generate.FnSignature;
import com.jozufozu.flywheel.backend.glsl.generate.GlslExpr;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public final class FlwPrograms {
	private FlwPrograms() {
	}

	private static void reload(ResourceManager resourceManager) {
		var sources = new ShaderSources(resourceManager);
		var preLoadStats = new CompilerStats();
		var loadChecker = new SourceLoader(sources, preLoadStats);

		var pipelineKeys = createPipelineKeys();
		var uniformComponent = createUniformComponent(loadChecker);
		List<SourceComponent> vertexComponents = List.of(createVertexMaterialComponent(loadChecker));
		List<SourceComponent> fragmentComponents = List.of(createFragmentMaterialComponent(loadChecker), createFogComponent(loadChecker), createCutoutComponent(loadChecker));

		InstancingPrograms.reload(sources, pipelineKeys, uniformComponent, vertexComponents, fragmentComponents);
		IndirectPrograms.reload(sources, pipelineKeys, uniformComponent, vertexComponents, fragmentComponents);

		if (preLoadStats.errored()) {
			Flywheel.LOGGER.error(preLoadStats.generateErrorLog());
		}
	}

	private static ImmutableList<PipelineProgramKey> createPipelineKeys() {
		ImmutableList.Builder<PipelineProgramKey> builder = ImmutableList.builder();
		for (Context context : Context.REGISTRY) {
			for (InstanceType<?> instanceType : InstanceType.REGISTRY) {
				builder.add(new PipelineProgramKey(instanceType, context));
			}
		}
		return builder.build();
	}

	private static UberShaderComponent createVertexMaterialComponent(SourceLoader loadChecker) {
		return UberShaderComponent.builder(Flywheel.rl("uber_material_vertex"))
				.materialSources(ShaderIndices.materialVertex()
						.all())
				.adapt(FnSignature.ofVoid("flw_materialVertex"))
				.switchOn(GlslExpr.variable("_flw_uberMaterialVertexIndex"))
				.build(loadChecker);
	}

	private static UberShaderComponent createFragmentMaterialComponent(SourceLoader loadChecker) {
		return UberShaderComponent.builder(Flywheel.rl("uber_material_fragment"))
				.materialSources(ShaderIndices.materialFragment()
						.all())
				.adapt(FnSignature.ofVoid("flw_materialFragment"))
				.switchOn(GlslExpr.variable("_flw_uberMaterialFragmentIndex"))
				.build(loadChecker);
	}

	private static UberShaderComponent createFogComponent(SourceLoader loadChecker) {
		return UberShaderComponent.builder(Flywheel.rl("uber_fog"))
				.materialSources(ShaderIndices.fog()
						.all())
				.adapt(FnSignature.create()
						.returnType("vec4")
						.name("flw_fogFilter")
						.arg("vec4", "color")
						.build(), GlslExpr.variable("color"))
				.switchOn(GlslExpr.variable("_flw_uberFogIndex"))
				.build(loadChecker);
	}

	private static UberShaderComponent createCutoutComponent(SourceLoader loadChecker) {
		return UberShaderComponent.builder(Flywheel.rl("uber_cutout"))
				.materialSources(ShaderIndices.cutout()
						.all())
				.adapt(FnSignature.create()
						.returnType("bool")
						.name("flw_discardPredicate")
						.arg("vec4", "color")
						.build(), GlslExpr.boolLiteral(false))
				.switchOn(GlslExpr.variable("_flw_uberCutoutIndex"))
				.build(loadChecker);
	}

	private static UniformComponent createUniformComponent(SourceLoader loadChecker) {
		return UniformComponent.builder(Flywheel.rl("uniforms"))
				.sources(ShaderUniforms.REGISTRY.getAll()
						.stream()
						.map(ShaderUniforms::uniformShader)
						.toList())
				.build(loadChecker);
	}

	public static class ResourceReloadListener implements ResourceManagerReloadListener {
		public static final ResourceReloadListener INSTANCE = new ResourceReloadListener();

		private ResourceReloadListener() {
		}

		@Override
		public void onResourceManagerReload(ResourceManager manager) {
			FlwPrograms.reload(manager);
		}
	}
}
