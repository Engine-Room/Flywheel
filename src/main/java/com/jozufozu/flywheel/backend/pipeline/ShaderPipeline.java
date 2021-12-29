package com.jozufozu.flywheel.backend.pipeline;

import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.core.shader.ContextAwareProgram;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.core.shader.spec.ProgramSpec;

/**
 * The main interface for compiling usable shaders from program specs.
 * @param <P> the type of the program that this pipeline compiles.
 */
public interface ShaderPipeline<P extends WorldProgram> {

	ContextAwareProgram<P> compile(ProgramSpec spec, VertexType vertexType);

}
