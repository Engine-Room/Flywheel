package com.jozufozu.flywheel.glsl.error;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import com.jozufozu.flywheel.glsl.SourceFile;
import com.jozufozu.flywheel.glsl.SourceLines;
import com.jozufozu.flywheel.glsl.error.lines.ErrorLine;
import com.jozufozu.flywheel.glsl.error.lines.FileLine;
import com.jozufozu.flywheel.glsl.error.lines.HeaderLine;
import com.jozufozu.flywheel.glsl.error.lines.NestedLine;
import com.jozufozu.flywheel.glsl.error.lines.SourceLine;
import com.jozufozu.flywheel.glsl.error.lines.SpanHighlightLine;
import com.jozufozu.flywheel.glsl.error.lines.TextLine;
import com.jozufozu.flywheel.glsl.span.Span;
import com.jozufozu.flywheel.util.ConsoleColors;
import com.jozufozu.flywheel.util.StringUtil;

import net.minecraft.resources.ResourceLocation;

public class ErrorBuilder {
	// set to false for testing
	@VisibleForTesting
	public static boolean CONSOLE_COLORS = true;

	private final List<ErrorLine> lines = new ArrayList<>();

	private ErrorBuilder() {

	}

	public static ErrorBuilder create() {
		return new ErrorBuilder();
	}

	public ErrorBuilder error(String msg) {
		return header(ErrorLevel.ERROR, msg);
	}

	public ErrorBuilder warn(String msg) {
		return header(ErrorLevel.WARN, msg);
	}

	public ErrorBuilder hint(String msg) {
		return header(ErrorLevel.HINT, msg);
	}

	public ErrorBuilder note(String msg) {
		return header(ErrorLevel.NOTE, msg);
	}

	public ErrorBuilder header(ErrorLevel level, String msg) {
		lines.add(new HeaderLine(level.toString(), msg));
		return this;
	}

	public ErrorBuilder extra(String msg) {
		lines.add(new TextLine(msg));
		return this;
	}

	public ErrorBuilder pointAtFile(SourceFile file) {
		return pointAtFile(file.name);
	}

	public ErrorBuilder pointAtFile(SourceLines source) {
		return pointAtFile(source.name);
	}

	public ErrorBuilder pointAtFile(ResourceLocation file) {
		return pointAtFile(file.toString());
	}

	public ErrorBuilder pointAtFile(String file) {
		lines.add(new FileLine(file));
		return this;
	}

	public ErrorBuilder hintIncludeFor(@Nullable Span span, String msg) {
		if (span == null) {
			return this;
		}

		var source = span.source();

		String builder = "add " + "#use " + '"' + source.name + '"' + ' ' + msg + "\n defined here:";

		header(ErrorLevel.HINT, builder);

		return this.pointAtFile(source)
				.pointAt(span, 0);
	}

	public ErrorBuilder pointAt(Span span) {
		return pointAt(span, 0);
	}

	public ErrorBuilder pointAt(Span span, int ctxLines) {
		if (span.lines() == 1) {
			SourceLines lines = span.source();

			int spanLine = span.firstLine();
			int firstCol = span.start()
					.col();
			int lastCol = span.end()
					.col();

			pointAtLine(lines, spanLine, ctxLines, firstCol, lastCol);
		}

		return this;
	}

	public ErrorBuilder pointAtLine(SourceLines lines, int spanLine, int ctxLines) {
		return pointAtLine(lines, spanLine, ctxLines, lines.lineStartColTrimmed(spanLine), lines.lineWidth(spanLine));
	}

	public ErrorBuilder pointAtLine(SourceLines lines, int spanLine, int ctxLines, int firstCol, int lastCol) {
		int firstLine = Math.max(0, spanLine - ctxLines);
		int lastLine = Math.min(lines.count(), spanLine + ctxLines);


		for (int i = firstLine; i <= lastLine; i++) {
			CharSequence line = lines.lineString(i);

			this.lines.add(SourceLine.numbered(i + 1, line.toString()));

			if (i == spanLine) {
				this.lines.add(new SpanHighlightLine(firstCol, lastCol));
			}
		}

		return this;
	}

	public String build() {
		Stream<String> lineStream = getLineStream();

		if (CONSOLE_COLORS) {
			lineStream = lineStream.map(line -> line + ConsoleColors.RESET);
		}

		return lineStream.collect(Collectors.joining("\n"));
	}

	@NotNull
	private Stream<String> getLineStream() {
		int maxMargin = calculateMargin();

		return lines.stream()
				.map(line -> addPaddingToLine(maxMargin, line));
	}

	private static String addPaddingToLine(int maxMargin, ErrorLine errorLine) {
		int neededMargin = errorLine.neededMargin();

		if (neededMargin >= 0) {
			return StringUtil.repeatChar(' ', maxMargin - neededMargin) + errorLine.build();
		} else {
			return errorLine.build();
		}
	}

	private int calculateMargin() {
		int maxMargin = -1;
		for (ErrorLine line : lines) {
			int neededMargin = line.neededMargin();

			if (neededMargin > maxMargin) {
				maxMargin = neededMargin;
			}
		}
		return maxMargin;
	}

	public void nested(ErrorBuilder err) {
		err.getLineStream()
				.map(NestedLine::new)
				.forEach(lines::add);
	}
}
