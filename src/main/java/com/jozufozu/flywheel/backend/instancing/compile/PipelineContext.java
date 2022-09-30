package com.jozufozu.flywheel.backend.instancing.compile;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.api.context.ContextShader;
import com.jozufozu.flywheel.api.pipeline.PipelineShader;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.uniform.UniformProvider;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.core.ComponentRegistry;
import com.jozufozu.flywheel.core.SourceComponent;

/**
 * Represents the entire context of a program's usage.
 *
 * @param vertexType    The vertexType the program should be adapted for.
 * @param structType    The instance shader to use.
 * @param contextShader The context shader to use.
 */
public record PipelineContext(VertexType vertexType, StructType<?> structType, ContextShader contextShader,
							  PipelineShader pipelineShader) {

	@NotNull
	public Set<UniformProvider> uniformProviders() {
		var fragmentComponents = getFragmentComponents();
		var vertexComponents = getVertexComponents();

		return Stream.concat(fragmentComponents.stream(), vertexComponents.stream())
				.map(SourceComponent::included)
				.flatMap(Collection::stream)
				.map(SourceComponent::name)
				.<UniformProvider>mapMulti((component, consumer) -> {
					var uniformProvider = ComponentRegistry.getUniformProvider(component);
					if (uniformProvider != null) {
						consumer.accept(uniformProvider);
					}
				})
				.collect(Collectors.toSet());
	}

	ImmutableList<SourceComponent> getVertexComponents() {
		var layout = vertexType.getLayoutShader()
				.getFile();
		var instanceAssembly = pipelineShader.assemble(vertexType, structType);
		var instance = structType.getInstanceShader()
				.getFile();
		var context = contextShader.getVertexShader();
		var pipeline = pipelineShader.vertex()
				.getFile();
		return ImmutableList.of(layout, instanceAssembly, instance, context, pipeline);
	}

	ImmutableList<SourceComponent> getFragmentComponents() {
		var context = contextShader.getFragmentShader();
		var pipeline = pipelineShader.fragment()
				.getFile();
		return ImmutableList.of(context, pipeline);
	}
}
