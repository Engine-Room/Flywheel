package com.jozufozu.flywheel.core.compile;

import java.util.HashMap;
import java.util.Map;

import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.source.FileResolution;

/**
 * A caching compiler.
 *
 * <p>
 *     This class is responsible for compiling programs on the fly. An instance of this class will keep a cache of
 *     compiled programs, and will only compile a program if it is not already in the cache.
 * </p>
 */
public class ProgramCompiler<P extends GlProgram> {

	protected final Map<ProgramContext, P> cache = new HashMap<>();
	private final GlProgram.Factory<P> factory;

	private final Template template;
	private final FileResolution header;

	public ProgramCompiler(GlProgram.Factory<P> factory, Template template, FileResolution header) {
		this.factory = factory;
		this.template = template;
		this.header = header;
	}

	/**
	 * Get or compile a spec to the given vertex type, accounting for all game state conditions specified by the spec.
	 *
	 * @param ctx The context of compilation.
	 * @return A compiled GlProgram.
	 */
	public P getProgram(ProgramContext ctx) {
		return cache.computeIfAbsent(ctx, this::compile);
	}

	public void invalidate() {
		cache.values().forEach(P::delete);
		cache.clear();
	}

	private P compile(ProgramContext ctx) {
		ShaderCompiler compiler = new ShaderCompiler(ctx, template, header);

		return new ProgramAssembler(compiler.name)
				.attachShader(compiler.compile(ShaderType.VERTEX))
				.attachShader(compiler.compile(ShaderType.FRAGMENT))
				.link()
				.deleteLinkedShaders()
				.build(this.factory);
	}

}
