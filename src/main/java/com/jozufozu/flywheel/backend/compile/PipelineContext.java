package com.jozufozu.flywheel.backend.compile;

import com.jozufozu.flywheel.api.context.Context;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.pipeline.Pipeline;
import com.jozufozu.flywheel.api.vertex.VertexType;

/**
 * Represents the entire context of a program's usage.
 *
 * @param vertexType    The vertexType the program should be adapted for.
 * @param instanceType  The instance shader to use.
 * @param contextShader The context shader to use.
 */
public record PipelineContext(VertexType vertexType, InstanceType<?> instanceType, Context contextShader,
							  Pipeline pipelineShader) {
}
