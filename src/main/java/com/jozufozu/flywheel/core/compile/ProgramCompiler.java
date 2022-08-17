package com.jozufozu.flywheel.core.compile;

import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.vertex.VertexType;
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
 * <p>
 *     A ProgramCompiler is also responsible for deleting programs and shaders on renderer reload.
 * </p>
 */
public class ProgramCompiler extends Memoizer<ProgramCompiler.Context, GlProgram> {

	public static final ProgramCompiler INSTANCE = new ProgramCompiler();

	private final VertexCompiler vertexCompiler;
	private final FragmentCompiler fragmentCompiler;

	private ProgramCompiler() {
		this.vertexCompiler = new VertexCompiler();
		this.fragmentCompiler = new FragmentCompiler();
	}

	/**
	 * Get or compile a spec to the given vertex type, accounting for all game state conditions specified by the spec.
	 *
	 * @param ctx The context of compilation.
	 * @return A compiled GlProgram.
	 */
	public GlProgram getProgram(ProgramCompiler.Context ctx) {
		return super.get(ctx);
	}

	@Override
	public void invalidate() {
		super.invalidate();
		vertexCompiler.invalidate();
		fragmentCompiler.invalidate();
	}

	@Override
	protected GlProgram _create(ProgramCompiler.Context ctx) {
		// TODO: try-catch here to prevent crashing if shaders failed to compile
		Material material = ctx.material;
		FileResolution instanceShader = ctx.instanceShader();
		ContextShader contextShader = ctx.contextShader;

		var vertex = new VertexCompiler.Context(ctx.vertexType(), instanceShader.getFile(), material.getVertexShader().getFile(),
				contextShader.getVertexShader());

		var fragment = new FragmentCompiler.Context(material.getFragmentShader().getFile(), contextShader.getFragmentShader());

		return new ProgramAssembler(instanceShader.getFileLoc())
				.attachShader(vertexCompiler.get(vertex))
				.attachShader(fragmentCompiler.get(fragment))
				.link()
				.build(contextShader.factory());
	}

	@Override
	protected void _destroy(GlProgram value) {
		value.delete();
	}

	public static void onReloadRenderers(ReloadRenderersEvent event) {
		INSTANCE.invalidate();
	}

	/**
	 * Represents the entire context of a program's usage.
	 *
	 * @param vertexType     The vertexType the program should be adapted for.
	 * @param material       The material shader to use.
	 * @param instanceShader The instance shader to use.
	 * @param contextShader  The context shader to use.
	 */
	public record Context(VertexType vertexType, Material material, FileResolution instanceShader,
						  ContextShader contextShader) {
	}
}
