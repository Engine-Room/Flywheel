package com.jozufozu.flywheel.backend.pipeline.error;

import java.util.Optional;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.pipeline.SourceFile;
import com.jozufozu.flywheel.backend.pipeline.parse.ShaderStruct;
import com.jozufozu.flywheel.backend.pipeline.span.Span;

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

	public static void generateMissingStruct(SourceFile file, Span vertexName) {
		Optional<Span> span = file.parent.index.getStructDefinitionsMatching(vertexName)
				.stream()
				.findFirst()
				.map(ShaderStruct::getName);
		ErrorBuilder builder = new ErrorBuilder();

		ErrorBuilder error = builder.error("struct not defined")
				.in(file)
				.pointAt(vertexName, 2)
				.hintIncludeFor(span.orElse(null));

		Backend.log.error(error.build());
	}
}
