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
import com.jozufozu.flywheel.glsl.ShaderSources;
import com.jozufozu.flywheel.glsl.SourceComponent;
import com.jozufozu.flywheel.glsl.generate.FnSignature;
import com.jozufozu.flywheel.glsl.generate.GlslExpr;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public class FlwPrograms {
	private FlwPrograms() {
	}

	public static void reload(ResourceManager resourceManager) {
		var empty = List.of(Flywheel.rl("api/fragment.glsl"), Flywheel.rl("api/vertex.glsl"));
		var sources = new ShaderSources(resourceManager, empty);

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

	private static UberShaderComponent createFragmentMaterialComponent(SourceLoader loadChecker) {
		return UberShaderComponent.builder(Flywheel.rl("uber_fragment_material"))
				.materialSources(ShaderIndices.materialFragment()
						.all())
				.adapt(FnSignature.ofVoid("flw_materialFragment"))
				.switchOn(GlslExpr.variable("_flw_materialFragmentID"))
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
				.switchOn(GlslExpr.variable("_flw_fogID"))
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
				.switchOn(GlslExpr.variable("_flw_cutoutID"))
				.build(loadChecker);
	}

	private static UberShaderComponent createVertexMaterialComponent(SourceLoader loadChecker) {
		return UberShaderComponent.builder(Flywheel.rl("vertex_material_adapter"))
				.materialSources(ShaderIndices.materialVertex()
						.all())
				.adapt(FnSignature.ofVoid("flw_materialVertex"))
				.switchOn(GlslExpr.variable("_flw_materialVertexID"))
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

	private static ImmutableList<PipelineProgramKey> createPipelineKeys() {
		ImmutableList.Builder<PipelineProgramKey> builder = ImmutableList.builder();
		for (Context context : Context.REGISTRY) {
			for (InstanceType<?> instanceType : InstanceType.REGISTRY) {
				builder.add(new PipelineProgramKey(instanceType, context));
			}
		}
		return builder.build();
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
