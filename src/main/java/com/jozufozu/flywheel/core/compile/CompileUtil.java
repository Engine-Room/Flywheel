package com.jozufozu.flywheel.core.compile;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jozufozu.flywheel.backend.gl.GLSLVersion;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;

public class CompileUtil {

	public static final Pattern vecType = Pattern.compile("^[biud]?vec([234])$");
	public static final Pattern matType = Pattern.compile("^mat([234])(?:x([234]))?$");

	protected static String generateHeader(GLSLVersion version, ShaderType type) {
		return "#version "
				+ version
				+ '\n'
				+ "#extension GL_ARB_explicit_attrib_location : enable\n"
				+ "#extension GL_ARB_conservative_depth : enable\n"
				+ type.getDefineStatement();
	}

	public static int getElementCount(String type) {
		Matcher vec = vecType.matcher(type);
		if (vec.find()) return Integer.parseInt(vec.group(1));

		Matcher mat = matType.matcher(type);
		if (mat.find()) {
			int n = Integer.parseInt(mat.group(1));

			String m = mat.group(2);

			if (m != null) return Integer.parseInt(m) * n;

			return n;
		}

		return 1;
	}

	public static int getAttributeCount(CharSequence type) {
		Matcher mat = matType.matcher(type);
		if (mat.find()) {
			return Integer.parseInt(mat.group(1));
		}

		return 1;
	}
}
