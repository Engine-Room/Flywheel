package com.jozufozu.flywheel.backend.pipeline;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.loading.ProtoProgram;
import com.jozufozu.flywheel.backend.pipeline.parse.ShaderStruct;
import com.jozufozu.flywheel.backend.pipeline.parse.StructField;

public interface ITemplate {
	void generateTemplateSource(StringBuilder builder, ShaderType type, SourceFile file);

	void attachAttributes(ProtoProgram program, SourceFile file);


	static void prefixFields(StringBuilder builder, ShaderStruct struct, String qualifier, String prefix) {
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

	static void assignFields(StringBuilder builder, ShaderStruct struct, String prefix1, String prefix2) {
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
