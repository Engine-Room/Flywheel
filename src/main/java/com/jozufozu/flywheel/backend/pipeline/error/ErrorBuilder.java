package com.jozufozu.flywheel.backend.pipeline.error;

import com.jozufozu.flywheel.backend.pipeline.SourceFile;
import com.jozufozu.flywheel.backend.pipeline.span.Span;

public class ErrorBuilder {

	private final StringBuilder internal = new StringBuilder();

	public ErrorBuilder header(CharSequence msg) {
		internal.append("error: ")
				.append(msg);
		return endLine();
	}

	public ErrorBuilder errorIn(SourceFile file) {
		internal.append("--> ")
				.append(file.name);
		return endLine();
	}

	public ErrorBuilder numberedLine(int no, CharSequence content) {
		return line(String.valueOf(no), content);
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

	public ErrorBuilder pointAt(Span span, int ctxLines) {
		SourceFile file = span.getSourceFile();

		if (span.lines() == 1) {

			int spanLine = span.firstLine();

			int firstLine = Math.max(0, spanLine - ctxLines);
			int lastLine = Math.min(file.getLineCount(), spanLine + ctxLines);


			int firstCol = span.getStart().getCol();
			int lastCol = span.getEnd().getCol();

			for (int i = firstLine; i <= lastLine; i++) {
				CharSequence line = file.getLine(i);

				numberedLine(i + 1, line);

				if (i == spanLine) {
					line(" ", generateUnderline(firstCol, lastCol));
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
