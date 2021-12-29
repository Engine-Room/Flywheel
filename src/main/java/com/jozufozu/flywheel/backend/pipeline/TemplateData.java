package com.jozufozu.flywheel.backend.pipeline;

import com.jozufozu.flywheel.backend.gl.shader.ShaderType;

public interface TemplateData {
	void vertexFooter(StringBuilder builder, ShaderCompiler file);
	void fragmentFooter(StringBuilder builder, ShaderCompiler file);

	/**
	 * Generate the necessary glue code here.
	 *
	 * @param builder The builder to generate the source into.
	 * @param type The shader stage glue code is needed for.
	 * @param file The SourceFile with user written code.
	 */
	default void generateFooter(StringBuilder builder, ShaderType type, ShaderCompiler file) {
		if (type == ShaderType.VERTEX) {
			vertexFooter(builder, file);
		} else if (type == ShaderType.FRAGMENT) {
			fragmentFooter(builder, file);
		}
	}
}
