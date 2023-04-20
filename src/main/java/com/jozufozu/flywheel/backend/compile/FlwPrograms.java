package com.jozufozu.flywheel.backend.compile;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.uniform.ShaderUniforms;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.compile.component.UniformComponent;
import com.jozufozu.flywheel.glsl.ShaderSources;
import com.jozufozu.flywheel.lib.context.Contexts;

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

		InstancingPrograms.reload(sources, pipelineKeys, uniformComponent);
		IndirectPrograms.reload(sources, pipelineKeys, uniformComponent);
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
