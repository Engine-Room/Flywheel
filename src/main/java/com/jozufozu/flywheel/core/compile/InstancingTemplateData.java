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
	public final ShaderFunction vertexMain;
	public final Span vertexName;
	public final Span instanceName;
	public final ShaderStruct instance;

	public InstancingTemplateData(SourceFile file) {
		this.file = file;

		Optional<ShaderFunction> vertexFunc = file.findFunction("vertex");

		if (vertexFunc.isEmpty()) {
			ErrorReporter.generateFileError(file, "could not find \"vertex\" function");
			throw new ShaderLoadingException();
		}

		vertexMain = vertexFunc.get();
		ImmutableList<Variable> vertexParams = vertexMain.getParameters();

		if (vertexParams.size() != 2) {
			ErrorReporter.generateSpanError(vertexMain.getArgs(), "instancing requires vertex function to have 2 arguments");
			throw new ShaderLoadingException();
		}

		Variable vertexParam = vertexParams.get(0);
		vertexName = vertexParam.type;

		boolean namedVertex = vertexParam.type
				.toString()
				.equals("Vertex");


		if (!(namedVertex && vertexParam.qualifier == Variable.Qualifier.INOUT)) {
			ErrorReporter.generateSpanError(vertexParam.qualifierSpan, "first parameter must be inout Vertex");
			throw new ShaderLoadingException();
		}

		instanceName = vertexParams.get(1).type;

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
					.append("a_i_")
					.append(field.name)
					.append(";\n");
			attributeBinding += CompileUtil.getAttributeCount(field.type);
		}
		template.append(String.format("""
						out vec4 v2f_color;
						out vec2 v2f_texCoords;
						out vec2 v2f_light;
						out float v2f_diffuse;

						void main() {
						    Vertex v = FLWCreateVertex();
						    %s i;
						    %s
						    vertex(v, i);
						    gl_Position = FLWVertex(v);
						    v.normal = normalize(v.normal);

						    v2f_color = v.color;
						    v2f_texCoords = v.texCoords;
						    v2f_light = v.light;
						    v2f_diffuse = diffuse(v.normal);
						    #if defined(DEBUG_NORMAL)
						    v2f_color = vec4(v.normal, 1.);
						    #endif
						}
						""",
				instanceName,
				assignFields(instance, "i.", "a_i_")
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
