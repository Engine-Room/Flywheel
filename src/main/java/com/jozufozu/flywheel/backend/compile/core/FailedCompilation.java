package com.jozufozu.flywheel.backend.compile.core;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.backend.glsl.SourceFile;
import com.jozufozu.flywheel.backend.glsl.SourceLines;
import com.jozufozu.flywheel.backend.glsl.error.ConsoleColors;
import com.jozufozu.flywheel.backend.glsl.error.ErrorBuilder;
import com.jozufozu.flywheel.backend.glsl.error.ErrorLevel;
import com.jozufozu.flywheel.backend.glsl.span.Span;
import com.jozufozu.flywheel.lib.util.StringUtil;

import net.minecraft.resources.ResourceLocation;

public class FailedCompilation {
	public static final ResourceLocation GENERATED_SOURCE_NAME = Flywheel.rl("generated_source");
	private static final Pattern PATTERN_ONE = Pattern.compile("(\\d+)\\((\\d+)\\) : (.*)");
	private static final Pattern PATTERN_TWO = Pattern.compile("(\\w+): (\\d+):(\\d+):(?: '(.+?)' :)?(.*)");
	private final List<SourceFile> files;
	private final SourceLines generatedSource;
	private final String errorLog;
	private final String shaderName;
	// Unused, but handy for debugging.
	private final String completeSource;

	public FailedCompilation(String shaderName, List<SourceFile> files, String generatedSource, String completeSource, String errorLog) {
		this.shaderName = shaderName;
		this.files = files;
		this.generatedSource = new SourceLines(GENERATED_SOURCE_NAME, generatedSource);
		this.completeSource = completeSource;
		this.errorLog = errorLog;
	}

	public String generateMessage() {
		return ConsoleColors.RED_BOLD_BRIGHT + "Failed to compile " + shaderName + ":\n" + errorString();
	}

	public String errorString() {
		return errorStream().map(ErrorBuilder::build)
				.collect(Collectors.joining("\n"));
	}

	@NotNull
	private Stream<ErrorBuilder> errorStream() {
		return errorLog.lines()
				.mapMulti(this::interpretLine);
	}

	private void interpretLine(String s, Consumer<ErrorBuilder> out) {
		if (s.isEmpty()) {
			return;
		}

		Matcher matcher;

		matcher = PATTERN_ONE.matcher(s);
		if (matcher.find()) {
			out.accept(interpretPattern1(matcher));
			return;
		}

		matcher = PATTERN_TWO.matcher(s);
		if (matcher.find()) {
			out.accept(interpretPattern2(matcher));
			return;
		}

		out.accept(ErrorBuilder.create()
				.error(s));
	}

	private ErrorBuilder interpretPattern1(Matcher matcher) {
		int fileId = Integer.parseInt(matcher.group(1));
		int lineNo = Integer.parseInt(matcher.group(2));
		var msg = StringUtil.trimPrefix(matcher.group(3), "error")
				.stripLeading();

		if (fileId == 0) {
			return interpretGeneratedError(ErrorLevel.ERROR, lineNo, msg);
		} else {
			return interpretSourceError(fileId, lineNo, msg);
		}
	}

	private ErrorBuilder interpretPattern2(Matcher matcher) {
		var errorLevel = parseErrorLevel(matcher.group(1));

		int fileId = Integer.parseInt(matcher.group(2));
		int lineNo = Integer.parseInt(matcher.group(3)) - 1;

		String span = matcher.group(4);

		var msg = matcher.group(5).trim();

		if (fileId == 0) {
			return interpretGeneratedError(errorLevel, lineNo, msg);
		} else {
			return interpretWithSpan(errorLevel, fileId, lineNo, span, msg);
		}
	}

	private ErrorBuilder interpretSourceError(int fileId, int lineNo, String msg) {
		var sourceFile = files.get(fileId - 1);
		Span span = sourceFile.getLineSpanNoWhitespace(lineNo);

		return ErrorBuilder.create()
				.error(msg)
				.pointAtFile(sourceFile)
				.pointAt(span, 1);
	}

	private ErrorBuilder interpretWithSpan(ErrorLevel errorLevel, int fileId, int lineNo, String span, String msg) {
		var sourceFile = files.get(fileId - 1);

		Span errorSpan;
		if (span != null) {
			errorSpan = sourceFile.getLineSpanMatching(lineNo, span);
		} else {
			errorSpan = sourceFile.getLineSpanNoWhitespace(lineNo);
		}

		return ErrorBuilder.create()
				.header(errorLevel, msg)
				.pointAtFile(sourceFile)
				.pointAt(errorSpan, 1);
	}

	private ErrorBuilder interpretGeneratedError(ErrorLevel errorLevel, int lineNo, String msg) {
		return ErrorBuilder.create()
				.header(errorLevel, msg)
				.pointAtFile("[in generated source]")
				.pointAtLine(generatedSource, lineNo, 1)
				.note("This generally indicates a bug in Flywheel, not your shader code.");
	}

	@NotNull
	private static ErrorLevel parseErrorLevel(String level) {
		return switch (level.toLowerCase(Locale.ROOT)) {
			case "error" -> ErrorLevel.ERROR;
			case "warning", "warn" -> ErrorLevel.WARN;
			default -> ErrorLevel.NOTE;
		};
	}
}
