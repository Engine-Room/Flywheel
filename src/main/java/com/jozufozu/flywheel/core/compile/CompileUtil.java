package com.jozufozu.flywheel.core.compile;

import com.jozufozu.flywheel.backend.gl.GLSLVersion;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;

public class CompileUtil {

	protected static String generateHeader(GLSLVersion version, ShaderType type) {
		return "#version "
				+ version
				+ '\n'
				+ "#extension GL_ARB_explicit_attrib_location : enable\n"
				+ "#extension GL_ARB_conservative_depth : enable\n"
				+ type.getDefineStatement();
	}
}
