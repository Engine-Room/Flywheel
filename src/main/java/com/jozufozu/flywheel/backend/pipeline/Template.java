package com.jozufozu.flywheel.backend.pipeline;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.backend.gl.GLSLVersion;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
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
public abstract class Template<D> {

	private final Map<SourceFile, D> metadata = new HashMap<>();

	private final Function<SourceFile, D> parser;

	protected Template(Function<SourceFile, D> parser) {
		this.parser = parser;
	}

	/**
	 * Generate the necessary glue code here.
	 *
	 * <p>
	 *     See {@link InstancingTemplate} and {@link OneShotTemplate} for examples.
	 * </p>
	 * @param builder The builder to generate the source into.
	 * @param type The shader stage glue code is needed for.
	 * @param file The SourceFile with user written code.
	 */
	public abstract void generateTemplateSource(StringBuilder builder, ShaderType type, SourceFile file);

	public abstract Collection<ShaderInput> getShaderInputs(SourceFile file);

	public D getMetadata(SourceFile file) {
		return metadata.computeIfAbsent(file, parser);
	}

	public GLSLVersion getVersion() {
		return GLSLVersion.V120;
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

	public static void assignFields(StringBuilder builder, ShaderStruct struct, String prefix1, String prefix2) {
		ImmutableList<StructField> fields = struct.getFields();

		for (StructField field : fields) {
			builder.append(prefix1)
					.append(field.name)
					.append(" = ")
					.append(prefix2)
					.append(field.name)
					.append(";\n");
		}
	}
}
