package com.jozufozu.flywheel.backend.instancing.compile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.jozufozu.flywheel.api.context.ContextShader;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.core.BackendTypes;
import com.jozufozu.flywheel.core.ComponentRegistry;
import com.jozufozu.flywheel.core.Components;
import com.jozufozu.flywheel.core.pipeline.SimplePipeline;

public class PipelineContextSet {
	static PipelineContextSet create() {
		var builder = new PipelineContextSet();
		for (SimplePipeline pipelineShader : BackendTypes.availablePipelineShaders()) {
			for (StructType<?> structType : ComponentRegistry.structTypes) {
				for (VertexType vertexType : ComponentRegistry.vertexTypes) {
					builder.add(vertexType, structType, Components.WORLD, pipelineShader);
				}
			}
		}
		return builder;
	}

	private final List<PipelineContext> contexts = new ArrayList<>();
	private final List<PipelineContext> contextView = Collections.unmodifiableList(contexts);

	PipelineContextSet() {
	}

	public List<PipelineContext> all() {
		return contextView;
	}

	public int size() {
		return contexts.size();
	}

	private void add(VertexType vertexType, StructType<?> structType, ContextShader world, SimplePipeline pipelineShader) {
		var ctx = new PipelineContext(vertexType, structType, world, pipelineShader);


		contexts.add(ctx);
	}
}
