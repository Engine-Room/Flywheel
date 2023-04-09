package com.jozufozu.flywheel.backend.compile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.jozufozu.flywheel.api.backend.Backend;
import com.jozufozu.flywheel.api.context.Context;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.pipeline.Pipeline;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.lib.context.Contexts;

public class PipelineContextSet {
	private final List<PipelineContext> contexts = new ArrayList<>();
	private final List<PipelineContext> contextView = Collections.unmodifiableList(contexts);

	PipelineContextSet() {
	}

	static PipelineContextSet create() {
		var builder = new PipelineContextSet();
		for (Pipeline pipelineShader : availablePipelineShaders()) {
			for (InstanceType<?> instanceType : InstanceType.REGISTRY) {
				for (VertexType vertexType : VertexType.REGISTRY) {
					builder.add(vertexType, instanceType, Contexts.WORLD, pipelineShader);
				}
			}
		}
		return builder;
	}

	private static Collection<Pipeline> availablePipelineShaders() {
		return Backend.REGISTRY.getAll()
				.stream()
				.filter(Backend::isSupported)
				.map(Backend::pipelineShader)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	public List<PipelineContext> all() {
		return contextView;
	}

	public int size() {
		return contexts.size();
	}

	private void add(VertexType vertexType, InstanceType<?> instanceType, Context world, Pipeline pipelineShader) {
		var ctx = new PipelineContext(vertexType, instanceType, world, pipelineShader);

		contexts.add(ctx);
	}
}
