package com.jozufozu.flywheel.core.compile;

import java.util.ArrayList;
import java.util.List;

import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.core.Templates;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.event.ReloadRenderersEvent;

/**
 * A caching compiler.
 *
 * <p>
 *     This class is responsible for compiling programs on the fly. An instance of this class will keep a cache of
 *     compiled programs, and will only compile a program if it is not already in the cache.
 * </p>
 */
public class ProgramCompiler<P extends GlProgram> extends Memoizer<ProgramContext, P> {

	private static final List<ProgramCompiler<?>> ALL_COMPILERS = new ArrayList<>();

	private final GlProgram.Factory<P> factory;
	private final VertexCompiler vertexCompiler;
	private final FragmentCompiler fragmentCompiler;

	public ProgramCompiler(GlProgram.Factory<P> factory, VertexCompiler vertexCompiler, FragmentCompiler fragmentCompiler) {
		this.factory = factory;
		this.vertexCompiler = vertexCompiler;
		this.fragmentCompiler = fragmentCompiler;

		ALL_COMPILERS.add(this);
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
		return new ProgramCompiler<>(factory, new VertexCompiler(template, header), new FragmentCompiler(Templates.FRAGMENT, header));
	}

	/**
	 * Get or compile a spec to the given vertex type, accounting for all game state conditions specified by the spec.
	 *
	 * @param ctx The context of compilation.
	 * @return A compiled GlProgram.
	 */
	public P getProgram(ProgramContext ctx) {
		return super.get(ctx);
	}

	@Override
	public void invalidate() {
		super.invalidate();
		vertexCompiler.invalidate();
		fragmentCompiler.invalidate();
	}

	@Override
	protected P _create(ProgramContext ctx) {
		return new ProgramAssembler(ctx.spec.name)
				.attachShader(vertexCompiler.get(new VertexCompiler.Context(ctx.spec.getVertexFile(), ctx.ctx, ctx.vertexType)))
				.attachShader(fragmentCompiler.get(new FragmentCompiler.Context(ctx.spec.getFragmentFile(), ctx.ctx, ctx.alphaDiscard)))
				.link()
				.build(this.factory);
	}

	@Override
	protected void _destroy(P value) {
		value.delete();
	}

	public static void invalidateAll(ReloadRenderersEvent event) {
		ALL_COMPILERS.forEach(ProgramCompiler::invalidate);
	}
}
