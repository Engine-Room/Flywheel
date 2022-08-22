package com.jozufozu.flywheel.core.source.error;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.core.source.ShaderLoadingException;
import com.jozufozu.flywheel.core.source.SourceFile;
import com.jozufozu.flywheel.core.source.parse.ShaderFunction;
import com.jozufozu.flywheel.core.source.parse.ShaderStruct;
import com.jozufozu.flywheel.core.source.span.Span;
import com.jozufozu.flywheel.util.FlwUtil;

public class ErrorReporter {

	private final List<ErrorBuilder> reportedErrors = new ArrayList<>();

	public void generateMissingStruct(SourceFile file, Span vertexName, CharSequence msg) {
		generateMissingStruct(file, vertexName, msg, "");
	}

	public void generateMissingStruct(SourceFile file, Span vertexName, CharSequence msg, CharSequence hint) {
		Optional<Span> span = file.parent.index.getStructDefinitionsMatching(vertexName)
				.stream()
				.findFirst()
				.map(ShaderStruct::getName);

		this.error(msg)
				.pointAtFile(file)
				.pointAt(vertexName, 1)
				.hintIncludeFor(span.orElse(null), hint);
	}

	public void generateMissingFunction(SourceFile file, CharSequence functionName, CharSequence msg) {
		generateMissingFunction(file, functionName, msg, "");
	}

	public void generateMissingFunction(SourceFile file, CharSequence functionName, CharSequence msg, CharSequence hint) {
		Optional<Span> span = file.parent.index.getFunctionDefinitionsMatching(functionName)
				.stream()
				.findFirst()
				.map(ShaderFunction::getName);

		this.error(msg)
				.pointAtFile(file)
				.hintIncludeFor(span.orElse(null), hint);
	}

	public ErrorBuilder generateFunctionArgumentCountError(String name, int requiredArguments, Span span) {
		var msg = '"' + name + "\" function must ";

		if (requiredArguments == 0) {
			msg += "not have any arguments";
		} else {
			msg += "have exactly " + requiredArguments + " argument" + (requiredArguments == 1 ? "" : "s");
		}

		return generateSpanError(span, msg);
	}

	public ErrorBuilder generateSpanError(Span span, String message) {
		SourceFile file = span.getSourceFile();

		return error(message)
				.pointAtFile(file)
				.pointAt(span, 2);
	}

	public ErrorBuilder generateFileError(SourceFile file, String message) {
		return error(message)
				.pointAtFile(file);
	}

	public ErrorBuilder error(CharSequence msg) {
		var out = ErrorBuilder.error(msg);
		reportedErrors.add(out);
		return out;
	}

	public boolean hasErrored() {
		return !reportedErrors.isEmpty();
	}

	public ShaderLoadingException dump() {
		var allErrors = reportedErrors.stream()
				.map(ErrorBuilder::build)
				.collect(Collectors.joining());

		return new ShaderLoadingException(allErrors);
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
