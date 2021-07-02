package com.jozufozu.flywheel.backend.pipeline;

import java.util.List;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.core.shader.IMultiProgram;
import com.jozufozu.flywheel.core.shader.WorldProgram;

public class WorldShaderPipeline<P extends WorldProgram> {

	@Nullable // TODO: temporary null return
	public P compile(SourceFile file) {

		PipelineProgramBuilder builder = new PipelineProgramBuilder();

		builder.includeAll(Includer.recurseIncludes(file));

		builder.include(file);


		return null;
	}
}
