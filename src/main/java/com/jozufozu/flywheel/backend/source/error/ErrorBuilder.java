package com.jozufozu.flywheel.backend.source.error;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.backend.source.SourceFile;
import com.jozufozu.flywheel.backend.source.span.Span;
import com.jozufozu.flywheel.util.FlwUtil;

public class ErrorBuilder {

	private final StringBuilder internal = new StringBuilder();

	public ErrorBuilder error(CharSequence msg) {
		internal.append("error: ")
				.append(msg);
		return endLine();
	}

	public ErrorBuilder note(CharSequence msg) {
		internal.append("note: ")
				.append(msg);
		return endLine();
	}

	public ErrorBuilder hint(CharSequence msg) {
		internal.append("hint: ")
				.append(msg);
		return endLine();
	}

	public ErrorBuilder in(SourceFile file) {
		internal.append("--> ")
				.append(file.name);
		return endLine();
	}

	public ErrorBuilder numberedLine(int no, CharSequence content) {
		return line(String.valueOf(no), content);
	}

	public ErrorBuilder numberedLine(int no, int width, CharSequence content) {
		String fmt = "%1$" + width + 's';
		return line(String.format(fmt, no), content);
	}

	public ErrorBuilder line(CharSequence leftColumn, CharSequence rightColumn) {

		internal.append(leftColumn)
				.append(" | ")
				.append(rightColumn);

		return endLine();
	}

	public ErrorBuilder endLine() {
		internal.append('\n');
		return this;
	}

	public ErrorBuilder hintIncludeFor(@Nullable Span span, CharSequence msg) {
		if (span == null) return this;

		hint("add " + span.getSourceFile().importStatement() + " " + msg)
				.in(span.getSourceFile())
				.pointAt(span, 1);

		return this;
	}

	public ErrorBuilder pointAt(Span span, int ctxLines) {

		SourceFile file = span.getSourceFile();

		if (span.lines() == 1) {

			int spanLine = span.firstLine();

			int firstLine = Math.max(0, spanLine - ctxLines);
			int lastLine = Math.min(file.getLineCount(), spanLine + ctxLines);

			int digits = FlwUtil.numDigits(lastLine);

			int firstCol = span.getStart().getCol();
			int lastCol = span.getEnd().getCol();

			for (int i = firstLine; i <= lastLine; i++) {
				CharSequence line = file.getLine(i);

				numberedLine(i + 1, digits, line);

				if (i == spanLine) {
					line(FlwUtil.repeatChar(' ', digits), generateUnderline(firstCol, lastCol));
				}
			}
		}

		return this;
	}

	public CharSequence build() {
		return internal;
	}

	public CharSequence generateUnderline(int firstCol, int lastCol) {
		StringBuilder line = new StringBuilder(lastCol);
		for (int i = 0; i < firstCol; i++) {
			line.append(' ');
		}

		for (int i = firstCol; i < lastCol; i++) {
			line.append('^');
		}

		return line;
	}
}
