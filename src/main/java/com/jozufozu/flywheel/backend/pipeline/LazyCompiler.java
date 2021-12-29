package com.jozufozu.flywheel.backend.pipeline;

import java.util.HashMap;
import java.util.Map;

import com.jozufozu.flywheel.api.shader.FlexibleShader;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.core.shader.ContextAwareProgram;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.core.shader.spec.ProgramSpec;

public class LazyCompiler<P extends WorldProgram> implements FlexibleShader<P> {

	private final ShaderPipeline<P> pipeline;
	private final ProgramSpec spec;

	private final Map<VertexType, ContextAwareProgram<P>> cache = new HashMap<>();

	public LazyCompiler(ShaderPipeline<P> pipeline, ProgramSpec spec) {

		this.pipeline = pipeline;
		this.spec = spec;
	}

	public void delete() {
		cache.values().forEach(ContextAwareProgram::delete);
	}

	@Override
	public P get(VertexType type) {
		return cache.computeIfAbsent(type, t -> pipeline.compile(spec, t)).get();
	}
}
