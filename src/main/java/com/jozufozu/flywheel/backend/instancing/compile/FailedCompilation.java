package com.jozufozu.flywheel.backend.instancing.compile;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.jozufozu.flywheel.core.source.SourceFile;
import com.jozufozu.flywheel.core.source.SourceLines;
import com.jozufozu.flywheel.core.source.error.ErrorBuilder;
import com.jozufozu.flywheel.core.source.span.Span;
import com.jozufozu.flywheel.util.ConsoleColors;
import com.jozufozu.flywheel.util.StringUtil;

public class FailedCompilation {
	private static final Pattern ERROR_LINE = Pattern.compile("(\\d+)\\((\\d+)\\) : (.*)");
	private final List<SourceFile> files;
	private final SourceLines generatedSource;
	private final String errorLog;
	private final String shaderName;

	public FailedCompilation(String shaderName, List<SourceFile> files, String generatedSource, String errorLog) {
		this.shaderName = shaderName;
		this.files = files;
		this.generatedSource = new SourceLines(generatedSource);
		this.errorLog = errorLog;
	}

	public String getMessage() {
		return ConsoleColors.RED_BOLD_BRIGHT + "Failed to compile " + shaderName + ":\n" + errorString();
	}

	public String errorString() {
		return errorStream().map(ErrorBuilder::build)
				.collect(Collectors.joining("\n"));
	}

	@NotNull
	private Stream<ErrorBuilder> errorStream() {
		return errorLog.lines()
				.map(this::interpretErrorLine);
	}

	private ErrorBuilder interpretErrorLine(String s) {
		Matcher matcher = ERROR_LINE.matcher(s);

		if (matcher.find()) {
			int fileId = Integer.parseInt(matcher.group(1));
			int lineNo = Integer.parseInt(matcher.group(2));
			var msg = StringUtil.trimPrefix(matcher.group(3), "error")
					.stripLeading();

			if (fileId == 0) {
				return interpretGeneratedError(lineNo, msg);
			} else {
				return interpretSourceError(fileId, lineNo, msg);
			}
		}
		return ErrorBuilder.create()
				.error(s);
	}

	private ErrorBuilder interpretSourceError(int fileId, int lineNo, String msg) {
		var sourceFile = files.get(fileId - 1);
		Span span = sourceFile.getLineSpanNoWhitespace(lineNo);

		return ErrorBuilder.create()
				.error(msg)
				.pointAtFile(sourceFile)
				.pointAt(span, 1);
	}

	private ErrorBuilder interpretGeneratedError(int lineNo, String msg) {
		return ErrorBuilder.create()
				.error(msg)
				.pointAtFile("[in generated source]")
				.pointAtLine(generatedSource, lineNo, 1)
				.note("This generally indicates a bug in Flywheel, not your shader code.");
	}
}
