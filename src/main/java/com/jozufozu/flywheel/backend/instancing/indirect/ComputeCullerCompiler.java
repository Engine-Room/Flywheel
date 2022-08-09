package com.jozufozu.flywheel.backend.instancing.indirect;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.backend.gl.GLSLVersion;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.core.Components;
import com.jozufozu.flywheel.core.compile.CompileUtil;
import com.jozufozu.flywheel.core.compile.Memoizer;
import com.jozufozu.flywheel.core.compile.ProgramAssembler;
import com.jozufozu.flywheel.core.compile.ShaderCompilationException;
import com.jozufozu.flywheel.core.source.CompilationContext;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.event.ReloadRenderersEvent;

public class ComputeCullerCompiler extends Memoizer<FileResolution, GlProgram> {

	public static final ComputeCullerCompiler INSTANCE = new ComputeCullerCompiler();

	private ComputeCullerCompiler() {
	}

	@Override
	protected GlProgram _create(FileResolution file) {

		var finalSource = new StringBuilder();
		CompilationContext context = new CompilationContext();

		finalSource.append(CompileUtil.generateHeader(GLSLVersion.V460, ShaderType.COMPUTE));
		finalSource.append(file.getFile()
				.generateFinalSource(context));

		finalSource.append(Components.Pipeline.INDIRECT_CULL.getFile()
				.generateFinalSource(context));

		try {
			var shader = new GlShader(finalSource.toString(), ShaderType.COMPUTE, ImmutableList.of(file.getFileLoc()));

			return new ProgramAssembler(file.getFileLoc()).attachShader(shader)
					.link()
					.build(GlProgram::new);
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
}
