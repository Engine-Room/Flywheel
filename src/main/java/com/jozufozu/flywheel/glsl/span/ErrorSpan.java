package com.jozufozu.flywheel.glsl.span;

import com.jozufozu.flywheel.glsl.SourceLines;

/**
 * Represents a (syntactically) malformed segment of code.
 */
public class ErrorSpan extends Span {
	public ErrorSpan(SourceLines in, int loc) {
		super(in, loc, loc);
	}

	public ErrorSpan(SourceLines in, int start, int end) {
		super(in, start, end);
	}

	public ErrorSpan(SourceLines in, CharPos start, CharPos end) {
		super(in, start, end);
	}

	@Override
	public Span subSpan(int from, int to) {
		return new ErrorSpan(in, start, end); // the sub-span of an error is an error in the same location
	}

	@Override
	public String get() {
		return "";
	}

	@Override
	public boolean isErr() {
		return true;
	}
}
