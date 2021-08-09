package com.jozufozu.flywheel.backend.pipeline;

import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.backend.pipeline.error.ErrorReporter;
import com.jozufozu.flywheel.backend.pipeline.parse.ShaderFunction;
import com.jozufozu.flywheel.backend.pipeline.parse.ShaderStruct;
import com.jozufozu.flywheel.backend.pipeline.parse.Variable;
import com.jozufozu.flywheel.backend.pipeline.span.Span;

public class InstanceTemplateData {

	public final SourceFile file;
	public final ShaderFunction vertexMain;
	public final ShaderFunction fragmentMain;
	public final Span interpolantName;
	public final Span vertexName;
	public final Span instanceName;
	public final ShaderStruct interpolant;
	public final ShaderStruct vertex;
	public final ShaderStruct instance;

	public InstanceTemplateData(SourceFile file) {
		this.file = file;

		Optional<ShaderFunction> vertexFunc = file.findFunction("vertex");
		Optional<ShaderFunction> fragmentFunc = file.findFunction("fragment");


		if (!fragmentFunc.isPresent()) {
			ErrorReporter.generateFileError(file, "could not find \"fragment\" function");
		}
		if (!vertexFunc.isPresent()) {
			ErrorReporter.generateFileError(file, "could not find \"vertex\" function");
		}

		if (!fragmentFunc.isPresent() || !vertexFunc.isPresent()) {
			throw new RuntimeException();
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
			throw new RuntimeException();
		}

		interpolantName = vertexMain.getType();
		vertexName = vertexParams.get(0)
				.typeName();
		instanceName = vertexParams.get(1)
				.typeName();

		Optional<ShaderStruct> maybeInterpolant = file.findStruct(interpolantName);
		Optional<ShaderStruct> maybeVertex = file.findStruct(vertexName);
		Optional<ShaderStruct> maybeInstance = file.findStruct(instanceName);

		if (!maybeVertex.isPresent()) {
			ErrorReporter.generateMissingStruct(file, vertexName);
		}

		if (!maybeInterpolant.isPresent()) {
			ErrorReporter.generateMissingStruct(file, interpolantName);
		}

		if (!maybeInstance.isPresent()) {
			ErrorReporter.generateMissingStruct(file, instanceName);
		}

		if (!maybeVertex.isPresent() || !maybeInterpolant.isPresent() || !maybeInstance.isPresent()) {
			throw new RuntimeException();
		}

		interpolant = maybeInterpolant.get();
		vertex = maybeVertex.get();
		instance = maybeInstance.get();
	}
}
