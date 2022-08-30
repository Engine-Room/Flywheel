package com.jozufozu.flywheel.core.compile;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.backend.gl.GLSLVersion;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.core.source.CompilationContext;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.event.ReloadRenderersEvent;

/**
 * Simple shader compiler that pulls no excessive tricks.<p>
 * Useful for writing experimental shaders or
 */
public class DebugCompiler extends Memoizer<DebugCompiler.Context, GlProgram> {

	public static final DebugCompiler INSTANCE = new DebugCompiler();

	private final ShaderCompiler shaderCompiler;

	private DebugCompiler() {
		this.shaderCompiler = new ShaderCompiler();
	}

	@Override
	public void invalidate() {
		super.invalidate();
		shaderCompiler.invalidate();
	}

	@Override
	protected GlProgram _create(DebugCompiler.Context ctx) {

		return new ProgramAssembler(ctx.vertex.getFileLoc())
				.attachShader(shaderCompiler.vertex(ctx.vertex))
				.attachShader(shaderCompiler.fragment(ctx.fragment))
				.link()
				.build(GlProgram::new);
	}

	@Override
	protected void _destroy(GlProgram value) {
		value.delete();
	}

	public static void invalidateAll(ReloadRenderersEvent ignored) {
		INSTANCE.invalidate();
	}

	public record Context(FileResolution vertex, FileResolution fragment) {
	}

	/**
	 * Handles compilation and deletion of vertex shaders.
	 */
	private static class ShaderCompiler extends Memoizer<ShaderCompiler.Context, GlShader> {

		public GlShader vertex(FileResolution source) {
			return get(new Context(source, ShaderType.VERTEX));
		}

		public GlShader fragment(FileResolution source) {
			return get(new Context(source, ShaderType.FRAGMENT));
		}

		@Override
		protected GlShader _create(Context ctx) {
			var index = new CompilationContext();

			StringBuilder source = new StringBuilder(CompileUtil.generateHeader(GLSLVersion.V420, ctx.type));

			var file = ctx.source.getFile();
			for (var include : file.flattenedImports) {
				source.append(include.source(index));
			}

			source.append(file.source(index));

			try {
				return new GlShader(source.toString(), ctx.type, ImmutableList.of(ctx.source.getFileLoc()));
			} catch (ShaderCompilationException e) {
				throw e.withErrorLog(index);
			}
		}

		@Override
		protected void _destroy(GlShader value) {
			value.delete();
		}

		public record Context(FileResolution source, ShaderType type) {
		}
	}
}
