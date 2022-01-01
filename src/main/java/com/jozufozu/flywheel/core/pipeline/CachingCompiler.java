package com.jozufozu.flywheel.core.pipeline;

import java.util.HashMap;
import java.util.Map;

import com.jozufozu.flywheel.backend.gl.shader.GlProgram;

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
	 * @param context The context of compilation.
	 * @return A compiled GlProgram.
	 */
	public P getProgram(CompilationContext context) {
		return cache.computeIfAbsent(context, this.pipeline::compile);
	}

	public void invalidate() {
		cache.values().forEach(P::delete);
		cache.clear();
	}
}
