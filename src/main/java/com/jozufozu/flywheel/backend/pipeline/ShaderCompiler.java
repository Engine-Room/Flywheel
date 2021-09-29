package com.jozufozu.flywheel.backend.pipeline;

import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.core.shader.GameStateProgram;
import com.jozufozu.flywheel.core.shader.spec.ProgramSpec;

/**
 * The main interface for compiling usable shaders from program specs.
 * @param <P> the type of the program that this pipeline compiles.
 */
public interface ShaderCompiler<P extends GlProgram> {

	GameStateProgram<P> compile(ProgramSpec spec);

}
