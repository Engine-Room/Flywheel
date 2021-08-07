package com.jozufozu.flywheel.backend.pipeline;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.pipeline.parse.ShaderFunction;
import com.jozufozu.flywheel.backend.pipeline.parse.ShaderStruct;
import com.jozufozu.flywheel.backend.pipeline.parse.StructField;
import com.jozufozu.flywheel.backend.pipeline.parse.Variable;

public class Template {

	public CharSequence footer(ShaderType type, SourceFile file) {
		switch (type) {
		case VERTEX:
			return vertexFooter(file);
		case FRAGMENT:
			return fragmentFooter(file);
		}

		return "";
	}

	public CharSequence vertexFooter(SourceFile file) {
		ShaderFunction vertexMain = file.getFunctions()
				.get("vertex");

		ImmutableList<Variable> parameters = vertexMain.getParameters();

		ShaderStruct interpolant = file.getStructs()
				.get(vertexMain.returnType());

		ShaderStruct vertex = file.getStructs()
				.get(parameters.get(0)
						.typeName()
						.get());

		ShaderStruct instance = file.getStructs()
				.get(parameters.get(1)
						.typeName()
						.get());

		StringBuilder template = new StringBuilder();

		prefixFields(template, vertex, "attribute", "a_v_");
		prefixFields(template, instance, "attribute", "a_i_");
		prefixFields(template, interpolant, "varying", "v2f_");

		template.append("void main() {\n");
		template.append(vertex.name)
				.append(" v;\n");
		assignFields(template, vertex, "v.", "a_v_");

		template.append(instance.name)
				.append(" i;\n");
		assignFields(template, instance, "i.", "a_i_");

		template.append(interpolant.name)
				.append(" o = ")
				.append(vertexMain.call("v", "i"))
				.append(";\n");

		assignFields(template, interpolant, "v2f_", "o.");

		template.append('}');

		return template;
	}

	public CharSequence fragmentFooter(SourceFile file) {
		ShaderFunction fragmentMain = file.getFunctions()
				.get("fragment");

		ImmutableList<Variable> parameters = fragmentMain.getParameters();

		ShaderStruct interpolant = file.getStructs()
				.get(parameters.get(0)
						.typeName()
						.get());

		StringBuilder template = new StringBuilder();

		prefixFields(template, interpolant, "varying", "v2f_");

		template.append("void main() {\n");
		template.append(interpolant.name)
				.append(" o;\n");
		assignFields(template, interpolant, "o.", "v2f_");

		template.append(fragmentMain.call("o"))
				.append(";\n");

		template.append('}');

		return template;
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
