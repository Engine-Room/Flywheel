package com.jozufozu.flywheel.backend.glsl.span;

import java.util.regex.Matcher;

import org.jetbrains.annotations.NotNull;

import com.jozufozu.flywheel.backend.glsl.SourceFile;
import com.jozufozu.flywheel.backend.glsl.SourceLines;

/**
 * A segment of code in a {@link SourceFile}.
 *
 * <p>
 *     Spans are used for pretty-printing errors.
 * </p>
 */
public abstract class Span implements CharSequence, Comparable<Span> {
	protected final SourceLines in;
	protected final CharPos start;
	protected final CharPos end;

	public Span(SourceLines in, int start, int end) {
		this(in, in.getCharPos(start), in.getCharPos(end));
	}

	public Span(SourceLines in, CharPos start, CharPos end) {
		this.in = in;
		this.start = start;
		this.end = end;
	}

	/**
	 * @return The file that contains this code segment.
	 */
	public SourceLines source() {
		return in;
	}

	public CharPos start() {
		return start;
	}

	public CharPos end() {
		return end;
	}

	/**
	 * @return the string index at the (inclusive) beginning of this code segment.
	 */
	public int startIndex() {
		return start.pos();
	}

	/**
	 * @return the string index at the (exclusive) end of this code segment.
	 */
	public int endIndex() {
		return end.pos();
	}

	/**
	 * @return true if this span has no width.
	 */
	public boolean isEmpty() {
		return start == end;
	}

	/**
	 * @return how many lines this span spans.
	 */
	public int lines() {
		return end.line() - start.line() + 1;
	}

	public int firstLine() {
		return start.line();
	}

	/**
	 * Get a span referring to a code segment inside this code segment.
	 */
	public abstract Span subSpan(int from, int to);

	/**
	 * @return the portion of code represented by this span.
	 */
	public abstract String get();

	public abstract boolean isErr();

	@Override
	public int length() {
		return endIndex() - startIndex();
	}

	@Override
	public char charAt(int index) {
		return in.charAt(start.pos() + index);
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return subSpan(start, end);
	}

	@Override
	public String toString() {
		return get();
	}

	public static Span fromMatcher(SourceLines src, Matcher m, int group) {
		return new StringSpan(src, m.start(group), m.end(group));
	}

	public static Span fromMatcher(Span superSpan, Matcher m, int group) {
		return superSpan.subSpan(m.start(group), m.end(group));
	}

	public static Span fromMatcher(SourceLines src, Matcher m) {
		return new StringSpan(src, m.start(), m.end());
	}

	public static Span fromMatcher(Span superSpan, Matcher m) {
		return superSpan.subSpan(m.start(), m.end());
	}

	@Override
	public int compareTo(@NotNull Span o) {
		return Integer.compareUnsigned(startIndex(), o.startIndex());
	}
}
