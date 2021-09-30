package com.jozufozu.flywheel.backend.pipeline;

import com.jozufozu.flywheel.core.shader.IMultiProgram;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.core.shader.spec.ProgramSpec;

/**
 * The main interface for compiling usable shaders from program specs.
 * @param <P> the type of the program that this pipeline compiles.
 */
public interface IShaderPipeline<P extends WorldProgram> {

	IMultiProgram<P> compile(ProgramSpec spec);

}
