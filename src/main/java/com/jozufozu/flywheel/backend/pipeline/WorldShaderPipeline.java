package com.jozufozu.flywheel.backend.pipeline;

import com.jozufozu.flywheel.core.shader.IMultiProgram;
import com.jozufozu.flywheel.core.shader.StateSensitiveMultiProgram;
import com.jozufozu.flywheel.core.shader.WorldProgram;

import javax.annotation.Nullable;

public class WorldShaderPipeline<P extends WorldProgram> {

	@Nullable // TODO: temporary null return
	public IMultiProgram<P> compile(SourceFile file) {

		return null;
	}
}
