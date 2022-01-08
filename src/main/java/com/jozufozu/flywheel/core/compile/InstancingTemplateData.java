package com.jozufozu.flywheel.core.compile;

import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.source.ShaderLoadingException;
import com.jozufozu.flywheel.backend.source.SourceFile;
import com.jozufozu.flywheel.backend.source.error.ErrorReporter;
import com.jozufozu.flywheel.backend.source.parse.ShaderFunction;
import com.jozufozu.flywheel.backend.source.parse.ShaderStruct;
import com.jozufozu.flywheel.backend.source.parse.StructField;
import com.jozufozu.flywheel.backend.source.parse.Variable;
import com.jozufozu.flywheel.backend.source.span.Span;

public class InstancingTemplateData implements TemplateData {

	public final SourceFile file;
	public final ShaderFunction vertexMain;
	public final ShaderFunction fragmentMain;
	public final Span interpolantName;
	public final Span vertexName;
	public final Span instanceName;
	public final ShaderStruct interpolant;
	public final ShaderStruct instance;

	public InstancingTemplateData(SourceFile file) {
		this.file = file;

		Optional<ShaderFunction> vertexFunc = file.findFunction("vertex");
		Optional<ShaderFunction> fragmentFunc = file.findFunction("fragment");

		if (fragmentFunc.isEmpty()) {
			ErrorReporter.generateMissingFunction(file, "fragment", "\"fragment\" function not defined");
		}
		if (vertexFunc.isEmpty()) {
			ErrorReporter.generateFileError(file, "could not find \"vertex\" function");
		}

		if (fragmentFunc.isEmpty() || vertexFunc.isEmpty()) {
			throw new ShaderLoadingException();
		}

		fragmentMain = fragmentFunc.get();
		vertexMain = vertexFunc.get();
		ImmutableList<Variable> parameters = fragmentMain.getParameters();
		ImmutableList<Variable> vertexParams = vertexMain.getParameters();

		if (parameters.size() != 1) {
			ErrorReporter.generateSpanError(fragmentMain.getArgs(), "instancing requires fragment function to have 1 argument");
		}

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

		interpolantName = parameters.get(0).type;
		instanceName = vertexParams.get(1).type;

		Optional<ShaderStruct> maybeInterpolant = file.findStruct(interpolantName);
		Optional<ShaderStruct> maybeInstance = file.findStruct(instanceName);

		if (maybeInterpolant.isEmpty()) {
			ErrorReporter.generateMissingStruct(file, interpolantName, "struct not defined");
		}

		if (maybeInstance.isEmpty()) {
			ErrorReporter.generateMissingStruct(file, instanceName, "struct not defined");
		}

		if (maybeInterpolant.isEmpty() || maybeInstance.isEmpty()) {
			throw new ShaderLoadingException();
		}

		interpolant = maybeInterpolant.get();
		instance = maybeInstance.get();
	}

	public void vertexFooter(StringBuilder template, ShaderCompiler shader) {
		ImmutableList<StructField> fields = instance.getFields();
		VertexType vertexType = shader.vertexType;

		int attributeBinding = vertexType.getLayout()
				.getAttributeCount();

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
			attributeBinding += TypeHelper.getAttributeCount(field.type);
		}
		Template.prefixFields(template, interpolant, "out", "v2f_");

		template.append(String.format("""
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
				Template.assignFields(instance, "i.", "a_i_")
		));
	}

	public void fragmentFooter(StringBuilder template, FileIndex shader) {
		Template.prefixFields(template, interpolant, "in", "v2f_");

		template.append(String.format("""
				void main() {
				    Fragment o;
					o.color = v2f_color;
					o.texCoords = v2f_texCoords;
					o.light = v2f_light;
					o.diffuse = v2f_diffuse;

				    vec4 color = %s;
				    FLWFinalizeColor(color);
				}
				""",
				fragmentMain.call("o")
		));
	}
}
