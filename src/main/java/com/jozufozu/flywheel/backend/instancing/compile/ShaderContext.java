package com.jozufozu.flywheel.backend.instancing.compile;

import java.util.List;

import com.jozufozu.flywheel.backend.gl.GLSLVersion;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.core.SourceComponent;

/**
 * @param glslVersion      The GLSL version to use.
 * @param sourceComponents A list of shader components to stitch together, in order.
 */
public record ShaderContext(GLSLVersion glslVersion, ShaderType shaderType, List<SourceComponent> sourceComponents) {

	public String generateHeader() {
		return CompileUtil.generateHeader(glslVersion, shaderType);
	}
}
