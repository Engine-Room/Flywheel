package com.jozufozu.flywheel.core.compile;

import java.util.ArrayList;
import java.util.List;

import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.gl.GLSLVersion;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.core.CoreShaderInfoMap;
import com.jozufozu.flywheel.core.shader.StateSnapshot;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.event.ReloadRenderersEvent;

/**
 * A caching compiler.
 *
 * <p>
 *     This class is responsible for compiling programs on the fly. An instance of this class will keep a cache of
 *     compiled programs, and will only compile a program if it is not already in the cache.
 * </p>
 * <p>
 *     A ProgramCompiler is also responsible for deleting programs and shaders on renderer reload.
 * </p>
 */
public class ProgramCompiler<P extends GlProgram> extends Memoizer<ProgramCompiler.Context, P> {

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
	public static <P extends GlProgram> ProgramCompiler<P> create(GlProgram.Factory<P> factory, FileResolution vertexContextShader, FileResolution fragmentContextShader, GLSLVersion glslVersion) {
		return new ProgramCompiler<>(factory, new VertexCompiler(vertexContextShader, glslVersion), new FragmentCompiler(fragmentContextShader, glslVersion));
	}

	/**
	 * Get or compile a spec to the given vertex type, accounting for all game state conditions specified by the spec.
	 *
	 * @param ctx The context of compilation.
	 * @return A compiled GlProgram.
	 */
	public P getProgram(Context ctx) {
		return super.get(ctx);
	}

	@Override
	public void invalidate() {
		super.invalidate();
		vertexCompiler.invalidate();
		fragmentCompiler.invalidate();
	}

	@Override
	protected P _create(Context ctx) {
		return new ProgramAssembler(ctx.instanceShader().getFileLoc())
				.attachShader(vertexCompiler.get(new VertexCompiler.Context(ctx.vertexType(), ctx.instanceShader().getFile(), ctx.vertexMaterialShader().getFile(), ctx.ctx())))
				.attachShader(fragmentCompiler.get(new FragmentCompiler.Context(ctx.fragmentMaterialShader().getFile(), ctx.alphaDiscard(), ctx.fogType(), ctx.ctx())))
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

	/**
	 * Represents the entire context of a program's usage.
	 *
	 * @param vertexType             The vertexType the program should be adapted for.
	 * @param instanceShader         The instance shader to use.
	 * @param vertexMaterialShader   The vertex material shader to use.
	 * @param fragmentMaterialShader The fragment material shader to use.
	 * @param alphaDiscard           Alpha threshold below which pixels are discarded.
	 * @param fogType                Which type of fog should be applied.
	 * @param ctx                    A snapshot of the game state.
	 */
	public record Context(VertexType vertexType, FileResolution instanceShader, FileResolution vertexMaterialShader,
						  FileResolution fragmentMaterialShader, float alphaDiscard, CoreShaderInfoMap.CoreShaderInfo.FogType fogType, StateSnapshot ctx) {
	}
}
