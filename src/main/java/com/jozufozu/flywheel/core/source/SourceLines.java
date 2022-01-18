package com.jozufozu.flywheel.core.source;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.core.source.span.CharPos;
import com.jozufozu.flywheel.util.StringUtil;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

public class SourceLines {

	private static final Pattern newLine = Pattern.compile("(\\r\\n|\\r|\\n)");

	/**
	 * 0-indexed line to char pos mapping.
	 */
	private final IntList lineStarts;

	/**
	 * 0-indexed line lookup
	 */
	private final ImmutableList<String> lines;

	public SourceLines(String source) {
		this.lineStarts = createLineLookup(source);
		this.lines = getLines(source, lineStarts);
	}

	public int getLineCount() {
		return lines.size();
	}

	public String getLine(int lineNo) {
		return lines.get(lineNo);
	}

	public int getLineStart(int lineNo) {

		return lineStarts.getInt(lineNo);
	}

	public CharPos getCharPos(int charPos) {
		int lineNo = 0;
		for (; lineNo < lineStarts.size(); lineNo++) {
			int ls = lineStarts.getInt(lineNo);

			if (charPos < ls) {
				break;
			}
		}

		lineNo -= 1;

		int lineStart = lineStarts.getInt(lineNo);

		return new CharPos(charPos, lineNo, charPos - lineStart);
	}

	public String printLinesWithNumbers() {
		StringBuilder builder = new StringBuilder();

		for (int i = 0, linesSize = lines.size(); i < linesSize; i++) {
			builder.append(String.format("%1$4s: ", i + 1))
					.append(lines.get(i))
					.append('\n');
		}

		return builder.toString();
	}

	/**
	 * Scan the source for line breaks, recording the position of the first character of each line.
	 * @param source
	 */
	private static IntList createLineLookup(String source) {
		IntList l = new IntArrayList();
		l.add(0); // first line is always at position 0

		Matcher matcher = newLine.matcher(source);

		while (matcher.find()) {
			l.add(matcher.end());
		}
		return l;
	}

	private static ImmutableList<String> getLines(String source, IntList lines) {
		ImmutableList.Builder<String> builder = ImmutableList.builder();

		for (int i = 1; i < lines.size(); i++) {
			int start = lines.getInt(i - 1);
			int end = lines.getInt(i);

			builder.add(StringUtil.trimEnd(source.substring(start, end)));
		}

		return builder.build();
	}
}
