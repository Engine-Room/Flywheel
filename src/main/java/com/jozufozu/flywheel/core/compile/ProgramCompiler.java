package com.jozufozu.flywheel.core.compile;

import java.util.ArrayList;
import java.util.List;

import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
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
	 * Creates a program compiler using provided templates and headers.
	 * @param factory A factory to add meaning to compiled programs.
	 * @param vertexContextShader The context shader to use when compiling vertex shaders.
	 * @param fragmentContextShader The context shader to use when compiling fragment shaders.
	 * @param <P> The type of program to compile.
	 * @return A program compiler.
	 */
	public static <P extends GlProgram> ProgramCompiler<P> create(GlProgram.Factory<P> factory, FileResolution vertexContextShader, FileResolution fragmentContextShader) {
		return new ProgramCompiler<>(factory, new VertexCompiler(vertexContextShader), new FragmentCompiler(fragmentContextShader));
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
		return new ProgramAssembler(ctx.instanceShader.getFileLoc())
				.attachShader(vertexCompiler.get(new VertexCompiler.Context(ctx.vertexType, ctx.instanceShader.getFile(), ctx.vertexMaterialShader.getFile(), ctx.ctx)))
				.attachShader(fragmentCompiler.get(new FragmentCompiler.Context(ctx.fragmentMaterialShader.getFile(), ctx.alphaDiscard, ctx.fogType, ctx.ctx)))
				.link()
				.build(this.factory);
	}

	@Override
	protected void _destroy(P value) {
		value.delete();
	}

	public static void invalidateAll(ReloadRenderersEvent ignored) {
		ALL_COMPILERS.forEach(ProgramCompiler::invalidate);
	}
}
