package com.jozufozu.flywheel.backend.instancing.indirect;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.core.compile.Memoizer;
import com.jozufozu.flywheel.core.compile.ProgramAssembler;
import com.jozufozu.flywheel.core.source.CompilationContext;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.event.ReloadRenderersEvent;

public class ComputeCompiler extends Memoizer<FileResolution, GlProgram> {

	public static final ComputeCompiler INSTANCE = new ComputeCompiler();

	private ComputeCompiler() {
	}

	@Override
	protected GlProgram _create(FileResolution file) {

		String source = file.getFile()
				.generateFinalSource(new CompilationContext());

		var shader = new GlShader(source, ShaderType.COMPUTE, ImmutableList.of(file.getFileLoc()));

		return new ProgramAssembler(file.getFileLoc())
				.attachShader(shader)
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
}
