package com.jozufozu.flywheel.core.source.error;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.core.source.FileIndex;
import com.jozufozu.flywheel.core.source.SourceFile;
import com.jozufozu.flywheel.core.source.SourceLines;
import com.jozufozu.flywheel.core.source.error.lines.ErrorLine;
import com.jozufozu.flywheel.core.source.error.lines.FileLine;
import com.jozufozu.flywheel.core.source.error.lines.HeaderLine;
import com.jozufozu.flywheel.core.source.error.lines.SourceLine;
import com.jozufozu.flywheel.core.source.error.lines.SpanHighlightLine;
import com.jozufozu.flywheel.core.source.error.lines.TextLine;
import com.jozufozu.flywheel.core.source.span.Span;
import com.jozufozu.flywheel.util.FlwUtil;

public class ErrorBuilder {

	private static final Pattern ERROR_LINE = Pattern.compile("(\\d+)\\((\\d+)\\) : (.*)");

	private final List<ErrorLine> lines = new ArrayList<>();

	public ErrorBuilder() {

	}

	public static ErrorBuilder error(CharSequence msg) {
		return new ErrorBuilder()
				.header(ErrorLevel.ERROR, msg);
	}

	public static ErrorBuilder compError(CharSequence msg) {
		return new ErrorBuilder()
				.extra(msg);
	}

	public static ErrorBuilder warn(CharSequence msg) {
		return new ErrorBuilder()
				.header(ErrorLevel.WARN, msg);
	}

	@Nullable
	public static ErrorBuilder fromLogLine(FileIndex env, String s) {
		Matcher matcher = ERROR_LINE.matcher(s);

		if (matcher.find()) {
			String fileId = matcher.group(1);
			String lineNo = matcher.group(2);
			String msg = matcher.group(3);
			Span span = env.getLineSpan(Integer.parseInt(fileId), Integer.parseInt(lineNo));
			return ErrorBuilder.compError(msg)
					.pointAtFile(span.getSourceFile())
					.pointAt(span, 1);
		} else {
			return null;
		}
	}

	public ErrorBuilder header(ErrorLevel level, CharSequence msg) {
		lines.add(new HeaderLine(level.toString(), msg));
		return this;
	}

	public ErrorBuilder extra(CharSequence msg) {
		lines.add(new TextLine(msg.toString()));
		return this;
	}

	public ErrorBuilder pointAtFile(SourceFile file) {
		lines.add(new FileLine(file.name.toString()));
		return this;
	}

	public ErrorBuilder hintIncludeFor(@Nullable Span span, CharSequence msg) {
		if (span == null) return this;
		SourceFile sourceFile = span.getSourceFile();

		String builder = "add " + sourceFile.importStatement() + ' ' + msg + "\n defined here:";

		header(ErrorLevel.HINT, builder);

		return this.pointAtFile(sourceFile)
				.pointAt(span, 0);
	}

	public ErrorBuilder pointAt(Span span, int ctxLines) {

		if (span.lines() == 1) {
			SourceLines lines = span.getSourceFile().lines;

			int spanLine = span.firstLine();

			int firstLine = Math.max(0, spanLine - ctxLines);
			int lastLine = Math.min(lines.getLineCount(), spanLine + ctxLines);

            int firstCol = span.getStart()
                    .col();
            int lastCol = span.getEnd()
                    .col();

			for (int i = firstLine; i <= lastLine; i++) {
				CharSequence line = lines.getLine(i);

				this.lines.add(SourceLine.numbered(i + 1, line.toString()));

				if (i == spanLine) {
					this.lines.add(new SpanHighlightLine(firstCol, lastCol));
				}
			}
		}

		return this;
	}

	public String build() {

		int maxLength = -1;
		for (ErrorLine line : lines) {
			int length = line.neededMargin();

			if (length > maxLength) maxLength = length;
		}

		StringBuilder builder = new StringBuilder();
		builder.append('\n');
		for (ErrorLine line : lines) {
			int length = line.neededMargin();

			builder.append(FlwUtil.repeatChar(' ', maxLength - length))
					.append(line.build())
					.append('\n');
		}

		return builder.toString();
	}
}
