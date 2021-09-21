package com.jozufozu.flywheel.backend.pipeline;

import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.backend.source.SourceFile;
import com.jozufozu.flywheel.backend.source.error.ErrorReporter;
import com.jozufozu.flywheel.backend.source.parse.ShaderFunction;
import com.jozufozu.flywheel.backend.source.parse.ShaderStruct;
import com.jozufozu.flywheel.backend.source.parse.Variable;
import com.jozufozu.flywheel.backend.source.span.Span;

public class OneShotProgramMetaData {

	public final SourceFile file;
	public final ShaderFunction vertexMain;
	public final Span interpolantName;
	public final Span vertexName;
	public final ShaderStruct interpolant;
	public final ShaderStruct vertex;
	public final ShaderFunction fragmentMain;

	public OneShotProgramMetaData(SourceFile file) {
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

		interpolantName = vertexMain.getType();
		vertexName = vertexParameters.get(0)
				.typeName();

		Optional<ShaderStruct> maybeInterpolant = file.findStruct(interpolantName);
		Optional<ShaderStruct> maybeVertex = file.findStruct(vertexName);

		if (maybeVertex.isEmpty())
			ErrorReporter.generateMissingStruct(file, vertexName, "struct not defined");

		if (maybeInterpolant.isEmpty())
			ErrorReporter.generateMissingStruct(file, interpolantName, "struct not defined");

		if (maybeVertex.isEmpty() || maybeInterpolant.isEmpty()) {
			throw new RuntimeException();
		}

		interpolant = maybeInterpolant.get();
		vertex = maybeVertex.get();
	}
}
