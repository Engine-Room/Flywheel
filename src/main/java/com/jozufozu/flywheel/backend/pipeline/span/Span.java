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

	public SourceFile getSourceFile() {
		return in;
	}

	public int getStart() {
		return start.getPos();
	}

	public int getEnd() {
		return end.getPos();
	}

	public boolean isEmpty() {
		return start == end;
	}

	public abstract Span subSpan(int from, int to);

	public abstract String get();

	public abstract boolean isErr();

	@Override
	public int length() {
		return end.getPos() - start.getPos();
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
