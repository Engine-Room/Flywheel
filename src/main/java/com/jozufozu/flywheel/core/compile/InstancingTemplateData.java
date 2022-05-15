package com.jozufozu.flywheel.core.compile;

import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.core.source.FileIndex;
import com.jozufozu.flywheel.core.source.ShaderLoadingException;
import com.jozufozu.flywheel.core.source.SourceFile;
import com.jozufozu.flywheel.core.source.error.ErrorReporter;
import com.jozufozu.flywheel.core.source.parse.ShaderFunction;
import com.jozufozu.flywheel.core.source.parse.ShaderStruct;
import com.jozufozu.flywheel.core.source.parse.StructField;
import com.jozufozu.flywheel.core.source.parse.Variable;
import com.jozufozu.flywheel.core.source.span.Span;

public class InstancingTemplateData implements VertexData {
	public final SourceFile file;
	public final ShaderFunction instanceVertex;
	public final Span instanceName;
	public final ShaderStruct instance;

	public InstancingTemplateData(SourceFile file) {
		this.file = file;

		Optional<ShaderFunction> maybeInstanceVertex = file.findFunction("flw_instanceVertex");

		if (maybeInstanceVertex.isEmpty()) {
			ErrorReporter.generateMissingFunction(file, "flw_instanceVertex", "\"flw_instanceVertex\" function not defined");
			throw new ShaderLoadingException();
		}

		instanceVertex = maybeInstanceVertex.get();
		ImmutableList<Variable> params = instanceVertex.getParameters();

		if (params.size() != 1) {
			ErrorReporter.generateSpanError(instanceVertex.getArgs(), "\"flw_contextFragment\" function must have exactly 1 argument");
			throw new ShaderLoadingException();
		}

		instanceName = params.get(0).type;
		Optional<ShaderStruct> maybeInstance = file.findStruct(instanceName);

		if (maybeInstance.isEmpty()) {
			ErrorReporter.generateMissingStruct(file, instanceName, "struct not defined");
			throw new ShaderLoadingException();
		}

		instance = maybeInstance.get();
	}

	@Override
	public String generateFooter(FileIndex shader, VertexType vertexType) {
		ImmutableList<StructField> fields = instance.getFields();

		int attributeBinding = vertexType.getLayout()
				.getAttributeCount();

		StringBuilder template = new StringBuilder();

		for (StructField field : fields) {
			template.append("layout(location = ")
					.append(attributeBinding)
					.append(") in")
					.append(' ')
					.append(field.type)
					.append(' ')
					.append("_flw_a_i_")
					.append(field.name)
					.append(";\n");
			attributeBinding += CompileUtil.getAttributeCount(field.type);
		}
		template.append('\n');

		template.append(String.format("""
						void main() {
							flw_layoutVertex();

							%s instance;
							%s
							flw_instanceVertex(instance);

							flw_contextVertex();
						}
						""",
				instanceName,
				assignFields(instance, "instance.", "_flw_a_i_")
		));

		return template.toString();
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
