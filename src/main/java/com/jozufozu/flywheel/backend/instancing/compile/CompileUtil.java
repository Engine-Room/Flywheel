package com.jozufozu.flywheel.backend.instancing.compile;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.jozufozu.flywheel.backend.gl.GLSLVersion;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.core.source.SourceFile;

public class CompileUtil {

	public static final Pattern vecType = Pattern.compile("^[biud]?vec([234])$");
	public static final Pattern matType = Pattern.compile("^mat([234])(?:x([234]))?$");

	public static String generateHeader(GLSLVersion version, ShaderType type) {
		return version.getVersionLine()
				+ type.getDefineStatement()
				+ '\n';
	}

	public static int getElementCount(String type) {
		Matcher vec = vecType.matcher(type);
		if (vec.find()) {
			return Integer.parseInt(vec.group(1));
		}

		Matcher mat = matType.matcher(type);
		if (mat.find()) {
			int n = Integer.parseInt(mat.group(1));

			String m = mat.group(2);

			if (m != null) {
				return Integer.parseInt(m) * n;
			}

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

    @NotNull
    public static String generateDebugName(SourceFile... stages) {
        return Stream.of(stages)
                .map(SourceFile::toString)
                .collect(Collectors.joining(" -> "));
    }
}
