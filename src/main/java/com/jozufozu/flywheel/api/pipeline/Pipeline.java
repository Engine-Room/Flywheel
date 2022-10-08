package com.jozufozu.flywheel.api.pipeline;

import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.gl.GLSLVersion;
import com.jozufozu.flywheel.core.SourceComponent;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.core.source.ShaderSources;

public interface Pipeline {

	GLSLVersion glslVersion();

	FileResolution vertex();

	FileResolution fragment();

	/**
	 * Generate the source component necessary to convert a packed {@link StructType} into its shader representation.
	 *
	 * @return A source component defining functions that unpack a representation of the given struct type.
	 */
	SourceComponent assemble(InstanceAssemblerContext context);

	record InstanceAssemblerContext(ShaderSources sources, VertexType vertexType, StructType<?> structType) {
	}
}
