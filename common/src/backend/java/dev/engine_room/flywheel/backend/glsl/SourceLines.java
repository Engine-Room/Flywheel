package dev.engine_room.flywheel.backend.glsl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;

import dev.engine_room.flywheel.backend.glsl.span.CharPos;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import net.minecraft.resources.ResourceLocation;

public class SourceLines implements CharSequence {
	private static final Pattern NEW_LINE = Pattern.compile("(\\r\\n|\\r|\\n)");

	public final ResourceLocation name;
	/**
	 * 0-indexed line to char pos mapping.
	 */
	private final IntList lineStarts;

	/**
	 * 0-indexed line lookup
	 */
	private final ImmutableList<String> lines;
	public final String raw;

	public SourceLines(ResourceLocation name, String raw) {
		this.name = name;
		this.raw = raw;
		this.lineStarts = createLineLookup(raw);
		this.lines = getLines(raw, lineStarts);
	}

	public int count() {
		return lines.size();
	}

	public String lineString(int lineNo) {
		return lines.get(lineNo);
	}

	public int lineStartIndex(int lineNo) {
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
	 */
	private static IntList createLineLookup(String source) {
		if (source.isEmpty()) {
			return IntLists.emptyList();
		}

		IntList l = new IntArrayList();
		l.add(0); // first line is always at position 0

		Matcher matcher = NEW_LINE.matcher(source);

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

			builder.add(source.substring(start, end)
					.stripTrailing());
		}

		return builder.build();
	}

	@Override
	public String toString() {
		return raw;
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return raw.subSequence(start, end);
	}

	@Override
	public char charAt(int i) {
		return raw.charAt(i);
	}

	@Override
	public int length() {
		return raw.length();
	}

	public int lineStartCol(int spanLine) {
		return 0;
	}

	public int lineWidth(int spanLine) {
		return lines.get(spanLine)
				.length();
	}

	public int lineStartColTrimmed(final int line) {
		final var lineString = lineString(line);
		final int end = lineString.length();

		int col = 0;
		while (col < end && Character.isWhitespace(charAt(col))) {
			col++;
		}
		return col;
	}

	public int lineStartPosTrimmed(final int line) {
		return lineStartIndex(line) + lineStartColTrimmed(line);
	}
}
