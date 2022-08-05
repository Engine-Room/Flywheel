package com.jozufozu.flywheel.backend.instancing.indirect;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.backend.gl.GLSLVersion;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.core.WorldProgram;
import com.jozufozu.flywheel.core.compile.CompileUtil;
import com.jozufozu.flywheel.core.compile.Memoizer;
import com.jozufozu.flywheel.core.compile.ProgramAssembler;
import com.jozufozu.flywheel.core.compile.ShaderCompilationException;
import com.jozufozu.flywheel.core.source.CompilationContext;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.core.source.ShaderLoadingException;
import com.jozufozu.flywheel.core.source.SourceFile;
import com.jozufozu.flywheel.event.ReloadRenderersEvent;

public class IndirectDrawCompiler extends Memoizer<IndirectDrawCompiler.Program, GlProgram> {
	public static final IndirectDrawCompiler INSTANCE = new IndirectDrawCompiler();

	private IndirectDrawCompiler() {
	}

	@Override
	protected GlProgram _create(IndirectDrawCompiler.Program program) {

		GlShader vertexShader = compile(program.vertex.getFile(), ShaderType.VERTEX);
		GlShader fragmentShader = compile(program.fragment.getFile(), ShaderType.FRAGMENT);

		return new ProgramAssembler(program.vertex.getFileLoc())
				.attachShader(vertexShader)
				.attachShader(fragmentShader)
				.link()
				.build(WorldProgram::new);
	}

	@NotNull
	private static GlShader compile(SourceFile file, ShaderType type) {
		var context = new CompilationContext();
		try {
			var header = CompileUtil.generateHeader(GLSLVersion.V460, type);
			var source = file.generateFinalSource(context);

			return new GlShader(header + source, type, ImmutableList.of(file.name));
		} catch (ShaderCompilationException e) {
			throw e.withErrorLog(context);
		}
	}

	@Override
	protected void _destroy(GlProgram value) {
		value.delete();
	}

	public static void invalidateAll(ReloadRenderersEvent ignored) {
		INSTANCE.invalidate();
	}

	public record Program(FileResolution vertex, FileResolution fragment) {

	}
}
