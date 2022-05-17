package com.jozufozu.flywheel.core.source.span;

import java.util.Optional;
import java.util.regex.Matcher;

import com.jozufozu.flywheel.core.source.SourceFile;
import com.jozufozu.flywheel.core.source.parse.ShaderFunction;
import com.jozufozu.flywheel.core.source.parse.ShaderStruct;

/**
 * A segment of code in a {@link SourceFile}.
 *
 * <p>
 *     Spans are used for pretty-printing errors.
 * </p>
 */
public abstract class Span implements CharSequence {

	protected final SourceFile in;
	protected final CharPos start;
	protected final CharPos end;

	public Span(SourceFile in, int start, int end) {
		this(in, in.lines.getCharPos(start), in.lines.getCharPos(end));
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

	public CharPos getStart() {
		return start;
	}

	public CharPos getEnd() {
		return end;
	}

	/**
	 * @return the string index at the (inclusive) beginning of this code segment.
	 */
	public int getStartPos() {
		return start.pos();
	}

	/**
	 * @return the string index at the (exclusive) end of this code segment.
	 */
	public int getEndPos() {
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
		return getEndPos() - getStartPos();
	}

	@Override
	public char charAt(int index) {
		return in.source.charAt(start.pos() + index);
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

	public Optional<ShaderStruct> findStruct() {
		if (isErr()) {
			return Optional.empty();
		}
		return in.findStruct(this);
	}

	public Optional<ShaderFunction> findFunction() {
		if (isErr()) {
			return Optional.empty();
		}
		return in.findFunction(this);
	}
}
