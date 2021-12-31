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

		CharSequence error = ErrorBuilder.error(message)
				.pointAtFile(file)
				.pointAt(span, 2)
				.build();

		Backend.LOGGER.error(error);
	}

	public static void generateFileError(SourceFile file, String message) {

		CharSequence error = ErrorBuilder.error(message)
				.pointAtFile(file)
				.build();

		Backend.LOGGER.error(error);
	}

	public static void generateMissingStruct(SourceFile file, Span vertexName, CharSequence msg) {
		generateMissingStruct(file, vertexName, msg, "");
	}

	public static void generateMissingStruct(SourceFile file, Span vertexName, CharSequence msg, CharSequence hint) {
		Optional<Span> span = file.parent.index.getStructDefinitionsMatching(vertexName)
				.stream()
				.findFirst()
				.map(ShaderStruct::getName);

		ErrorBuilder error = ErrorBuilder.error(msg)
				.pointAtFile(file)
				.pointAt(vertexName, 2)
				.hintIncludeFor(span.orElse(null), hint);

		Backend.LOGGER.error(error.build());
	}

	public static void generateMissingFunction(SourceFile file, CharSequence functionName, CharSequence msg) {
		generateMissingFunction(file, functionName, msg, "");
	}

	public static void generateMissingFunction(SourceFile file, CharSequence functionName, CharSequence msg, CharSequence hint) {
		Optional<Span> span = file.parent.index.getFunctionDefinitionsMatching(functionName)
				.stream()
				.findFirst()
				.map(ShaderFunction::getName);

		ErrorBuilder error = ErrorBuilder.error(msg)
				.pointAtFile(file)
				.hintIncludeFor(span.orElse(null), hint);

		Backend.LOGGER.error(error.build());
	}
}
