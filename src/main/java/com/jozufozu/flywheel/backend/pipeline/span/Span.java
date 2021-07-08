package com.jozufozu.flywheel.backend.pipeline.span;

import java.util.regex.Matcher;

import com.jozufozu.flywheel.backend.pipeline.SourceFile;

/**
 * A span of code in a {@link SourceFile}.
 */
public abstract class Span implements CharSequence {

	protected final SourceFile in;
	protected final CharPos start;
	protected final CharPos end;

	public Span(SourceFile in, int start, int end) {
		this(in, in.getCharPos(start), in.getCharPos(end));
	}

	public Span(SourceFile in, CharPos start, CharPos end) {
		this.in = in;
		this.start = start;
		this.end = end;
	}

	/**
	 * @return The file that contains this code segment.
	 */
	public SourceFile getSourceFile() {
		return in;
	}

	/**
	 * @return the string index at the (inclusive) beginning of this code segment.
	 */
	public int getStartPos() {
		return start.getPos();
	}

	/**
	 * @return the string index at the (exclusive) end of this code segment.
	 */
	public int getEndPos() {
		return end.getPos();
	}

	/**
	 * @return true if this span has no width.
	 */
	public boolean isEmpty() {
		return start == end;
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
		return getEndPos() - getStartPos();
	}

	@Override
	public char charAt(int index) {
		return in.getSource().charAt(start.getPos() + index);
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return subSpan(start, end);
	}

	@Override
	public String toString() {
		return get();
	}

	public static Span fromMatcher(SourceFile src, Matcher m, int group) {
		return new StringSpan(src, m.start(group), m.end(group));
	}

	public static Span fromMatcher(Span superSpan, Matcher m, int group) {
		return superSpan.subSpan(m.start(group), m.end(group));
	}

	public static Span fromMatcher(SourceFile src, Matcher m) {
		return new StringSpan(src, m.start(), m.end());
	}

	public static Span fromMatcher(Span superSpan, Matcher m) {
		return superSpan.subSpan(m.start(), m.end());
	}
}
