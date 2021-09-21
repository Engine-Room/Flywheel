package com.jozufozu.flywheel.backend.source.error;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.backend.source.SourceFile;
import com.jozufozu.flywheel.backend.source.SourceLines;
import com.jozufozu.flywheel.backend.source.error.lines.ErrorLine;
import com.jozufozu.flywheel.backend.source.error.lines.FileLine;
import com.jozufozu.flywheel.backend.source.error.lines.HeaderLine;
import com.jozufozu.flywheel.backend.source.error.lines.SourceLine;
import com.jozufozu.flywheel.backend.source.error.lines.SpanHighlightLine;
import com.jozufozu.flywheel.backend.source.span.Span;
import com.jozufozu.flywheel.util.FlwUtil;

public class ErrorBuilder {

	private final List<ErrorLine> lines = new ArrayList<>();

	private final Level level;

	public ErrorBuilder(Level level, CharSequence msg) {
		this.level = level;

		lines.add(new HeaderLine(level.toString(), msg));
	}

	public static ErrorBuilder error(CharSequence msg) {
		return new ErrorBuilder(Level.ERROR, msg);
	}

	public static ErrorBuilder warn(CharSequence msg) {
		return new ErrorBuilder(Level.WARN, msg);
	}

	public ErrorBuilder pointAtFile(SourceFile file) {
		lines.add(new FileLine(file.name.toString()));
		return this;
	}

	public ErrorBuilder hintIncludeFor(@Nullable Span span, CharSequence msg) {
		if (span == null) return this;

		String builder = "add " + span.getSourceFile()
				.importStatement() + ' ' + msg;

		lines.add(new HeaderLine("hint", builder));

		return this.pointAtFile(span.getSourceFile())
				.pointAt(span, 1);
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

	public CharSequence build() {

		int maxLength = -1;
		for (ErrorLine line : lines) {
			int length = line.neededMargin();

			if (length > maxLength) maxLength = length;
		}

		StringBuilder builder = new StringBuilder();
		for (ErrorLine line : lines) {
			int length = line.neededMargin();

			builder.append(FlwUtil.repeatChar(' ', maxLength - length))
					.append(line.build())
					.append('\n');
		}

		return builder;
	}
}
