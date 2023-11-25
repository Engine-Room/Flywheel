package com.jozufozu.flywheel.backend.compile;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.context.Context;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.uniform.ShaderUniforms;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.compile.component.MaterialAdapterComponent;
import com.jozufozu.flywheel.backend.compile.component.UniformComponent;
import com.jozufozu.flywheel.backend.compile.core.CompilerStats;
import com.jozufozu.flywheel.glsl.ShaderSources;
import com.jozufozu.flywheel.glsl.generate.FnSignature;
import com.jozufozu.flywheel.glsl.generate.GlslExpr;
import com.jozufozu.flywheel.lib.material.MaterialIndices;

import net.minecraft.server.packs.resources.ResourceManager;

public class FlwPrograms {
	private FlwPrograms() {
	}

	public static void reload(ResourceManager resourceManager) {
		var sources = new ShaderSources(resourceManager);

		var preLoadStats = new CompilerStats();
		var loadChecker = new SourceLoader(sources, preLoadStats);

		var pipelineKeys = createPipelineKeys();
		var uniformComponent = createUniformComponent(loadChecker);
		var vertexMaterialComponent = createVertexMaterialComponent(loadChecker);
		var fragmentMaterialComponent = createFragmentMaterialComponent(loadChecker);

		InstancingPrograms.reload(sources, pipelineKeys, uniformComponent, vertexMaterialComponent, fragmentMaterialComponent);
		IndirectPrograms.reload(sources, pipelineKeys, uniformComponent, vertexMaterialComponent, fragmentMaterialComponent);

		if (preLoadStats.errored()) {
			Flywheel.LOGGER.error(preLoadStats.generateErrorLog());
		}
	}

	private static MaterialAdapterComponent createFragmentMaterialComponent(SourceLoader loadChecker) {
		return MaterialAdapterComponent.builder(Flywheel.rl("fragment_material_adapter"))
				.materialSources(MaterialIndices.getAllFragmentShaders())
				.adapt(FnSignature.ofVoid("flw_materialFragment"))
				.adapt(FnSignature.create()
						.returnType("bool")
						.name("flw_discardPredicate")
						.arg("vec4", "color")
						.build(), GlslExpr.boolLiteral(false))
				.adapt(FnSignature.create()
						.returnType("vec4")
						.name("flw_fogFilter")
						.arg("vec4", "color")
						.build(), GlslExpr.variable("color"))
				.switchOn(GlslExpr.variable("_flw_materialFragmentID"))
				.build(loadChecker);
	}

	private static MaterialAdapterComponent createVertexMaterialComponent(SourceLoader loadChecker) {
		return MaterialAdapterComponent.builder(Flywheel.rl("vertex_material_adapter"))
				.materialSources(MaterialIndices.getAllVertexShaders())
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
		// TODO: ubershader'd contexts?
		for (Context context : Context.REGISTRY) {
			for (InstanceType<?> instanceType : InstanceType.REGISTRY) {
				for (VertexType vertexType : VertexType.REGISTRY) {
					builder.add(new PipelineProgramKey(vertexType, instanceType, context));
				}
			}
		}
		return builder.build();
	}
}
