package com.jozufozu.flywheel.core.pipeline;

import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.backend.source.ShaderLoadingException;
import com.jozufozu.flywheel.backend.source.SourceFile;
import com.jozufozu.flywheel.backend.source.error.ErrorReporter;
import com.jozufozu.flywheel.backend.source.parse.ShaderFunction;
import com.jozufozu.flywheel.backend.source.parse.ShaderStruct;
import com.jozufozu.flywheel.backend.source.parse.Variable;
import com.jozufozu.flywheel.backend.source.span.Span;

public class OneShotTemplateData implements TemplateData {

	public final SourceFile file;
	public final ShaderFunction vertexMain;
	public final Span interpolantName;
	public final ShaderStruct interpolant;
	public final ShaderFunction fragmentMain;

	public OneShotTemplateData(SourceFile file) {
		this.file = file;

		Optional<ShaderFunction> maybeVertexMain = file.findFunction("vertex");
		Optional<ShaderFunction> maybeFragmentMain = file.findFunction("fragment");

		if (maybeVertexMain.isEmpty()) {
			ErrorReporter.generateFileError(file, "could not find \"vertex\" function");
		}

		if (maybeFragmentMain.isEmpty()) {
			ErrorReporter.generateMissingFunction(file, "fragment", "\"fragment\" function not defined");
		}

		if (maybeVertexMain.isEmpty() || maybeFragmentMain.isEmpty()) {
			throw new RuntimeException();
		}

		vertexMain = maybeVertexMain.get();
		fragmentMain = maybeFragmentMain.get();
		ImmutableList<Variable> fragmentParameters = fragmentMain.getParameters();
		ImmutableList<Variable> vertexParameters = vertexMain.getParameters();

		if (vertexParameters.size() != 1) {
			ErrorReporter.generateSpanError(vertexMain.getArgs(), "a basic model requires vertex function to have one argument");
		}

		if (fragmentParameters.size() != 1) {
			ErrorReporter.generateSpanError(fragmentMain.getArgs(), "fragment function must have exactly one argument");
		}

		if (vertexParameters.size() != 1 || fragmentParameters.size() != 1) {
			throw new RuntimeException();
		}

		Variable vertexParam = vertexMain.getParameters().get(0);

		boolean namedVertex = vertexParam.type
				.toString()
				.equals("Vertex");

		if (!(namedVertex && vertexParam.qualifier == Variable.Qualifier.INOUT)) {
			ErrorReporter.generateSpanError(vertexParam.qualifierSpan, "first parameter must be inout Vertex");
			throw new ShaderLoadingException();
		}

		interpolantName = fragmentMain.getParameters().get(0).type;

		Optional<ShaderStruct> maybeInterpolant = file.findStruct(interpolantName);

		if (maybeInterpolant.isEmpty()) {
			ErrorReporter.generateMissingStruct(file, interpolantName, "struct not defined");

			throw new RuntimeException();
		}

		interpolant = maybeInterpolant.get();
	}

    public void vertexFooter(StringBuilder template, ShaderCompiler file) {
		Template.prefixFields(template, interpolant, "out", "v2f_");

		template.append("""
						void main() {
						    Vertex v = FLWCreateVertex();
						    vertex(v);
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
						""");
    }

	public void fragmentFooter(StringBuilder template, ShaderCompiler file) {
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
