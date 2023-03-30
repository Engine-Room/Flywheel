package com.jozufozu.flywheel.backend.compile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.jozufozu.flywheel.api.component.ComponentRegistry;
import com.jozufozu.flywheel.api.context.Context;
import com.jozufozu.flywheel.api.pipeline.Pipeline;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.lib.backend.BackendTypes;
import com.jozufozu.flywheel.lib.context.Contexts;

public class PipelineContextSet {
	static PipelineContextSet create() {
		var builder = new PipelineContextSet();
		for (Pipeline pipelineShader : BackendTypes.availablePipelineShaders()) {
			for (StructType<?> structType : ComponentRegistry.structTypes) {
				for (VertexType vertexType : ComponentRegistry.vertexTypes) {
					builder.add(vertexType, structType, Contexts.WORLD, pipelineShader);
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

	private void add(VertexType vertexType, StructType<?> structType, Context world, Pipeline pipelineShader) {
		var ctx = new PipelineContext(vertexType, structType, world, pipelineShader);


		contexts.add(ctx);
	}
}
