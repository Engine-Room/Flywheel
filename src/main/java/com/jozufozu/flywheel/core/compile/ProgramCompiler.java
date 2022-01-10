package com.jozufozu.flywheel.core.compile;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.source.FileResolution;
import com.jozufozu.flywheel.core.Templates;

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
	private final Function<ProgramContext, GlShader> vertexCompiler;
	private final Function<ProgramContext, GlShader> fragmentCompiler;

	public ProgramCompiler(GlProgram.Factory<P> factory, Function<ProgramContext, GlShader> vertexCompiler, Function<ProgramContext, GlShader> fragmentCompiler) {
		this.factory = factory;
		this.vertexCompiler = vertexCompiler;
		this.fragmentCompiler = fragmentCompiler;
	}

	/**
	 * Creates a program compiler using this template.
	 * @param template The vertex template to use.
	 * @param factory A factory to add meaning to compiled programs.
	 * @param header The header file to use for the program.
	 * @param <P> The type of program to compile.
	 * @return A program compiler.
	 */
	public static <T extends VertexData, P extends GlProgram> ProgramCompiler<P> create(Template<T> template, GlProgram.Factory<P> factory, FileResolution header) {
		return new ProgramCompiler<>(factory, ctx -> ShaderCompiler.compileVertex(ctx, template, header), ctx -> ShaderCompiler.compileFragment(ctx, Templates.FRAGMENT, header));
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

		return new ProgramAssembler(ctx.spec().name)
				.attachShader(vertexCompiler.apply(ctx))
				.attachShader(fragmentCompiler.apply(ctx))
				.link()
				.deleteLinkedShaders()
				.build(this.factory);
	}

}
