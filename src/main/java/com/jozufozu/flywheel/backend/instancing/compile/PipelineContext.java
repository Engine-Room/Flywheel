package com.jozufozu.flywheel.backend.instancing.compile;

import com.jozufozu.flywheel.api.context.ContextShader;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.core.pipeline.SimplePipeline;

/**
 * Represents the entire context of a program's usage.
 *
 * @param vertexType    The vertexType the program should be adapted for.
 * @param structType    The instance shader to use.
 * @param contextShader The context shader to use.
 */
public record PipelineContext(VertexType vertexType, StructType<?> structType, ContextShader contextShader,
							  SimplePipeline pipelineShader) {
}
