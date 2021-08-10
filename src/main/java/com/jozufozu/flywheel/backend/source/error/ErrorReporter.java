package com.jozufozu.flywheel.backend.source.error;

import java.util.Optional;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.source.SourceFile;
import com.jozufozu.flywheel.backend.source.parse.ShaderFunction;
import com.jozufozu.flywheel.backend.source.parse.ShaderStruct;
import com.jozufozu.flywheel.backend.source.span.Span;

public class ErrorReporter {

	public static void generateSpanError(Span span, String message) {
		SourceFile file = span.getSourceFile();

		ErrorBuilder builder = new ErrorBuilder();

		CharSequence error = builder.error(message)
				.in(file)
				.pointAt(span, 2)
				.build();

		Backend.log.error(error);
	}

	public static void generateFileError(SourceFile file, String message) {

		ErrorBuilder builder = new ErrorBuilder();

		CharSequence error = builder.error(message)
				.in(file)
				.build();

		Backend.log.error(error);
	}

	public static void generateMissingStruct(SourceFile file, Span vertexName, CharSequence msg) {
		generateMissingStruct(file, vertexName, msg, "");
	}

	public static void generateMissingStruct(SourceFile file, Span vertexName, CharSequence msg, CharSequence hint) {
		Optional<Span> span = file.parent.index.getStructDefinitionsMatching(vertexName)
				.stream()
				.findFirst()
				.map(ShaderStruct::getName);
		ErrorBuilder builder = new ErrorBuilder();

		ErrorBuilder error = builder.error(msg)
				.in(file)
				.pointAt(vertexName, 2)
				.hintIncludeFor(span.orElse(null), hint);

		Backend.log.error(error.build());
	}

	public static void generateMissingFunction(SourceFile file, CharSequence functionName, CharSequence msg) {
		generateMissingFunction(file, functionName, msg, "");
	}

	public static void generateMissingFunction(SourceFile file, CharSequence functionName, CharSequence msg, CharSequence hint) {
		Optional<Span> span = file.parent.index.getFunctionDefinitionsMatching(functionName)
				.stream()
				.findFirst()
				.map(ShaderFunction::getName);
		ErrorBuilder builder = new ErrorBuilder();

		ErrorBuilder error = builder.error(msg)
				.in(file)
				.hintIncludeFor(span.orElse(null), hint);

		Backend.log.error(error.build());
	}
}
