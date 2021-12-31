package com.jozufozu.flywheel.core.pipeline;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.backend.gl.GLSLVersion;
import com.jozufozu.flywheel.backend.source.SourceFile;
import com.jozufozu.flywheel.backend.source.parse.ShaderStruct;
import com.jozufozu.flywheel.backend.source.parse.StructField;

/**
 * A class that generates glsl glue code given a SourceFile.
 *
 * <p>
 *     Shader files are written somewhat abstractly. Subclasses of Template handle those abstractions, using SourceFile
 *     metadata to generate shader code that OpenGL can use to call into our shader programs.
 * </p>
 * @param <D> Holds metadata, generates errors.
 */
public class Template<D extends TemplateData> {

	private final Map<SourceFile, D> metadata = new HashMap<>();

	private final Function<SourceFile, D> reader;
	private final GLSLVersion glslVersion;

	public Template(GLSLVersion glslVersion, Function<SourceFile, D> reader) {
		this.reader = reader;
		this.glslVersion = glslVersion;
	}

	public D getMetadata(SourceFile file) {
		// lazily read files, cache results
		return metadata.computeIfAbsent(file, reader);
	}

	public GLSLVersion getVersion() {
		return glslVersion;
	}

	public static void prefixFields(StringBuilder builder, ShaderStruct struct, String qualifier, String prefix) {
		ImmutableList<StructField> fields = struct.getFields();

		for (StructField field : fields) {
			builder.append(qualifier)
					.append(' ')
					.append(field.type)
					.append(' ')
					.append(prefix)
					.append(field.name)
					.append(";\n");
		}
	}

	public static StringBuilder assignFields(ShaderStruct struct, String prefix1, String prefix2) {
		ImmutableList<StructField> fields = struct.getFields();

		StringBuilder builder = new StringBuilder();

		for (StructField field : fields) {
			builder.append(prefix1)
					.append(field.name)
					.append(" = ")
					.append(prefix2)
					.append(field.name)
					.append(";\n");
		}

		return builder;
	}
}
