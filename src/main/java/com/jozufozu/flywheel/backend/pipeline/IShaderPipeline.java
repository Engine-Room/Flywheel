package com.jozufozu.flywheel.backend.pipeline;

import com.jozufozu.flywheel.core.shader.IMultiProgram;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.core.shader.spec.ProgramSpec;

public interface IShaderPipeline<P extends WorldProgram> {

	IMultiProgram<P> compile(ProgramSpec spec);

}
