package com.jozufozu.flywheel.core.pipeline;

import java.util.HashMap;
import java.util.Map;

import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.core.shader.spec.ProgramSpec;

/**
 * Lazily compiles shader programs, caching the results.
 *
 * @param <P> The class that the PipelineCompiler outputs.
 */
public class CachingCompiler<P extends GlProgram> {
	protected final Map<CompilationContext, P> cache = new HashMap<>();
	private final PipelineCompiler<P> pipeline;

	public CachingCompiler(PipelineCompiler<P> pipeline) {
		this.pipeline = pipeline;
	}

	/**
	 * Get or compile a spec to the given vertex type, accounting for all game state conditions specified by the spec.
	 *
	 * @param spec The ProgramSpec to target.
	 * @param vertexType The VertexType to target.
	 * @return A compiled GlProgram.
	 */
	public P getProgram(ProgramSpec spec, VertexType vertexType) {
		return cache.computeIfAbsent(new CompilationContext(vertexType, spec, spec.getCurrentStateID()), this.pipeline::compile);
	}

	public void invalidate() {
		cache.values().forEach(P::delete);
		cache.clear();
	}
}
