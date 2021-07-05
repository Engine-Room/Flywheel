package com.jozufozu.flywheel.backend.pipeline;

import java.util.List;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.backend.ShaderSources;
import com.jozufozu.flywheel.core.shader.IMultiProgram;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.core.shader.spec.ProgramSpec;
import com.jozufozu.flywheel.core.shader.spec.ProgramState;

public class WorldShaderPipeline<P extends WorldProgram> {

	private final ShaderSources sources;

	public WorldShaderPipeline(ShaderSources sources) {
		this.sources = sources;
	}

	@Nullable // TODO: temporary null return
	public IMultiProgram<P> compile(ProgramSpec spec) {

		SourceFile file = sources.source(spec.vert);

		return compile(file, spec.getStates());
	}

	@Nullable
	public IMultiProgram<P> compile(SourceFile file, List<ProgramState> variants) {
		PipelineProgramBuilder builder = new PipelineProgramBuilder();

		builder.includeAll(Includer.recurseIncludes(file));

		builder.include(file);

		builder.build();

		return null;
	}
}
