package com.jozufozu.flywheel.api.pipeline;

import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.gl.GLSLVersion;
import com.jozufozu.flywheel.glsl.ShaderSources;
import com.jozufozu.flywheel.glsl.SourceComponent;

import net.minecraft.resources.ResourceLocation;

public interface Pipeline {

	GLSLVersion glslVersion();

	ResourceLocation vertexShader();

	ResourceLocation fragmentShader();

	/**
	 * Generate the source component necessary to convert a packed {@link StructType} into its shader representation.
	 *
	 * @return A source component defining functions that unpack a representation of the given struct type.
	 */
	SourceComponent assemble(InstanceAssemblerContext context);

	record InstanceAssemblerContext(ShaderSources sources, VertexType vertexType, StructType<?> structType) {
	}
}
