package com.jozufozu.flywheel.backend.pipeline;

import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.backend.pipeline.error.ErrorReporter;
import com.jozufozu.flywheel.backend.pipeline.parse.ShaderFunction;
import com.jozufozu.flywheel.backend.pipeline.parse.ShaderStruct;
import com.jozufozu.flywheel.backend.pipeline.parse.Variable;
import com.jozufozu.flywheel.backend.pipeline.span.Span;

public class OneShotData {

	public final SourceFile file;
	public final ShaderFunction vertexMain;
	public final Span interpolantName;
	public final Span vertexName;
	public final ShaderStruct interpolant;
	public final ShaderStruct vertex;
	public final ShaderFunction fragmentMain;

	public OneShotData(SourceFile file) {
		this.file = file;

		Optional<ShaderFunction> maybeVertexMain = file.findFunction("vertex");
		Optional<ShaderFunction> maybeFragmentMain = file.findFunction("fragment");

		if (!maybeVertexMain.isPresent()) {
			ErrorReporter.generateFileError(file, "could not find \"vertex\" function");
			throw new RuntimeException();
		}

		if (!maybeFragmentMain.isPresent()) {
			ErrorReporter.generateFileError(file, "could not find \"fragment\" function");
			throw new RuntimeException();
		}

		vertexMain = maybeVertexMain.get();
		fragmentMain = maybeFragmentMain.get();
		ImmutableList<Variable> parameters = fragmentMain.getParameters();
		ImmutableList<Variable> vertexParameters = vertexMain.getParameters();

		if (vertexParameters.size() != 1) {
			ErrorReporter.generateSpanError(vertexMain.getArgs(), "a basic model requires vertex function to have one argument");
			throw new RuntimeException();
		}

		if (parameters.size() != 1) {
			ErrorReporter.generateSpanError(fragmentMain.getArgs(), "instancing requires fragment function to have 1 argument");
			throw new RuntimeException();
		}


		interpolantName = vertexMain.getType();
		vertexName = vertexParameters.get(0)
				.typeName();

		Optional<ShaderStruct> maybeInterpolant = file.findStruct(interpolantName);
		Optional<ShaderStruct> maybeVertex = file.findStruct(vertexName);

		if (!maybeVertex.isPresent())
			ErrorReporter.generateMissingStruct(file, vertexName);

		if (!maybeInterpolant.isPresent())
			ErrorReporter.generateMissingStruct(file, interpolantName);

		if (!maybeVertex.isPresent() || !maybeInterpolant.isPresent()) {
			throw new RuntimeException();
		}

		interpolant = maybeInterpolant.get();
		vertex = maybeVertex.get();
	}
}
