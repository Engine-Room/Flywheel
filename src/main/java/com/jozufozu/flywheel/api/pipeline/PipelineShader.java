package com.jozufozu.flywheel.api.pipeline;

import java.util.function.Function;

import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.backend.gl.GLSLVersion;
import com.jozufozu.flywheel.core.SourceComponent;
import com.jozufozu.flywheel.core.source.FileResolution;

public record PipelineShader(GLSLVersion glslVersion, FileResolution vertex, FileResolution fragment,
							 InstanceAssemblerFactory factory) {

	/**
	 * Generate the source component necessary to convert a packed {@link StructType} into its shader representation.
	 *
	 * @param structType The struct type to convert.
	 * @return A source component defining functions that unpack a representation of the given struct type.
	 */
	public SourceComponent assemble(StructType<?> structType) {
		return factory.apply(structType);
	}

	public interface InstanceAssemblerFactory extends Function<StructType<?>, SourceComponent> {

	}
}
