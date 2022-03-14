package com.jozufozu.flywheel.core.source.error;

import java.util.List;
import java.util.Optional;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.core.source.SourceFile;
import com.jozufozu.flywheel.core.source.parse.ShaderFunction;
import com.jozufozu.flywheel.core.source.parse.ShaderStruct;
import com.jozufozu.flywheel.core.source.span.Span;
import com.jozufozu.flywheel.util.FlwUtil;

public class ErrorReporter {

	public static void generateSpanError(Span span, String message) {
		SourceFile file = span.getSourceFile();

		String error = ErrorBuilder.error(message)
				.pointAtFile(file)
				.pointAt(span, 2)
				.build();

		Backend.LOGGER.error(error);
	}

	public static void generateFileError(SourceFile file, String message) {

		String error = ErrorBuilder.error(message)
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
				.pointAt(vertexName, 1)
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

	public static void printLines(CharSequence source) {
		String string = source.toString();

		List<String> lines = string.lines()
				.toList();

		int size = lines.size();

		int maxWidth = FlwUtil.numDigits(size) + 1;

		StringBuilder builder = new StringBuilder().append('\n');

		for (int i = 0; i < size; i++) {

			builder.append(i)
					.append(FlwUtil.repeatChar(' ', maxWidth - FlwUtil.numDigits(i)))
					.append("| ")
					.append(lines.get(i))
					.append('\n');
		}

		Flywheel.LOGGER.error(builder.toString());
	}

}
