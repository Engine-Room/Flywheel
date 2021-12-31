package com.jozufozu.flywheel.core.pipeline;

import com.jozufozu.flywheel.backend.gl.shader.GlProgram;

/**
 * The main interface for compiling usable shaders from program specs.
 * @param <P> the type of the program that this pipeline compiles.
 */
public interface PipelineCompiler<P extends GlProgram> {

	P compile(CompilationContext vertexType);

}
