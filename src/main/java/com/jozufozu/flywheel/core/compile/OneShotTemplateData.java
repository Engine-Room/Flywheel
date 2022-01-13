package com.jozufozu.flywheel.core.compile;

import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.core.source.FileIndex;
import com.jozufozu.flywheel.core.source.ShaderLoadingException;
import com.jozufozu.flywheel.core.source.SourceFile;
import com.jozufozu.flywheel.core.source.error.ErrorReporter;
import com.jozufozu.flywheel.core.source.parse.ShaderFunction;
import com.jozufozu.flywheel.core.source.parse.Variable;

public class OneShotTemplateData implements VertexData {

	public final SourceFile file;
	public final ShaderFunction vertexMain;

	public OneShotTemplateData(SourceFile file) {
		this.file = file;

		Optional<ShaderFunction> maybeVertexMain = file.findFunction("vertex");

		if (maybeVertexMain.isEmpty()) {
			ErrorReporter.generateFileError(file, "could not find \"vertex\" function");
			throw new RuntimeException();
		}

		vertexMain = maybeVertexMain.get();
		ImmutableList<Variable> vertexParameters = vertexMain.getParameters();

		if (vertexParameters.size() != 1) {
			ErrorReporter.generateSpanError(vertexMain.getArgs(), "a basic model requires vertex function to have one argument");
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
	}

	@Override
    public String generateFooter(FileIndex file, VertexType vertexType) {
		return """
				out vec4 v2f_color;
				out vec2 v2f_texCoords;
				out vec2 v2f_light;
				out float v2f_diffuse;

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
				""";
    }
}
