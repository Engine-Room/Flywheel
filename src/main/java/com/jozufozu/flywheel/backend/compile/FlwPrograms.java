package com.jozufozu.flywheel.backend.compile;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.uniform.ShaderUniforms;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.compile.component.MaterialAdapterComponent;
import com.jozufozu.flywheel.backend.compile.component.UniformComponent;
import com.jozufozu.flywheel.glsl.ShaderSources;
import com.jozufozu.flywheel.glsl.generate.FnSignature;
import com.jozufozu.flywheel.glsl.generate.GlslExpr;
import com.jozufozu.flywheel.lib.context.Contexts;
import com.jozufozu.flywheel.lib.material.MaterialIndices;

import net.minecraft.server.packs.resources.ResourceManager;

public class FlwPrograms {
	private FlwPrograms() {
	}

	public static void reload(ResourceManager resourceManager) {
		var sources = new ShaderSources(resourceManager);
		var pipelineKeys = createPipelineKeys();
		var uniformComponent = UniformComponent.builder(Flywheel.rl("uniforms"))
				.sources(ShaderUniforms.REGISTRY.getAll()
						.stream()
						.map(ShaderUniforms::uniformShader)
						.toList())
				.build(sources);

		var vertexMaterialComponent = MaterialAdapterComponent.builder(Flywheel.rl("vertex_material_adapter"))
				.materialSources(MaterialIndices.getAllVertexShaders())
				.adapt(FnSignature.ofVoid("flw_materialVertex"))
				.switchOn(GlslExpr.variable("_flw_materialVertexID"))
				.build(sources);

		var fragmentMaterialComponent = MaterialAdapterComponent.builder(Flywheel.rl("fragment_material_adapter"))
				.materialSources(MaterialIndices.getAllFragmentShaders())
				.adapt(FnSignature.ofVoid("flw_materialFragment"))
				.adapt(FnSignature.create()
						.returnType("bool")
						.name("flw_discardPredicate")
						.arg("vec4", "color")
						.build(), GlslExpr.literal(false))
				.adapt(FnSignature.create()
						.returnType("vec4")
						.name("flw_fogFilter")
						.arg("vec4", "color")
						.build(), GlslExpr.variable("color"))
				.switchOn(GlslExpr.variable("_flw_materialFragmentID"))
				.build(sources);

		InstancingPrograms.reload(sources, pipelineKeys, uniformComponent, vertexMaterialComponent, fragmentMaterialComponent);
		IndirectPrograms.reload(sources, pipelineKeys, uniformComponent, vertexMaterialComponent, fragmentMaterialComponent);
	}

	private static ImmutableList<PipelineProgramKey> createPipelineKeys() {
		ImmutableList.Builder<PipelineProgramKey> builder = ImmutableList.builder();
		for (InstanceType<?> instanceType : InstanceType.REGISTRY) {
			for (VertexType vertexType : VertexType.REGISTRY) {
				builder.add(new PipelineProgramKey(vertexType, instanceType, Contexts.WORLD));
			}
		}
		return builder.build();
	}
}
